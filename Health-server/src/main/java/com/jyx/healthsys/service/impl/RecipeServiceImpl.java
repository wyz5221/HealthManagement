package com.jyx.healthsys.service.impl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jyx.healthsys.entity.Recipe;
import com.jyx.healthsys.mapper.RecipeMapper;
import com.jyx.healthsys.service.IRecipeService;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecipeServiceImpl extends ServiceImpl<RecipeMapper, Recipe> implements IRecipeService {

    @Resource
    private RecipeMapper recipeMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-path}")
    private String accessPath;

    @Override
    public List<Recipe> getRecipesByMealType(String mealType) {
        QueryWrapper<Recipe> wrapper = new QueryWrapper<>();
        wrapper.eq("meal_type", mealType);
        return recipeMapper.selectList(wrapper);
    }

    @Transactional
    @Override
    public boolean addRecipe(Recipe recipe) {
        QueryWrapper<Recipe> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", recipe.getName());
        List<Recipe> list = this.baseMapper.selectList(queryWrapper);

        if (list.isEmpty()) {
            this.baseMapper.insert(recipe);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateRecipe(Recipe recipe) {
        this.baseMapper.updateById(recipe);
    }

    @Override
    public Recipe getRecipeById(Integer id) {
        return this.baseMapper.selectById(id);
    }

    @Override
    public void deleteRecipeById(Integer id) {
        this.baseMapper.deleteById(id);
    }


    @Transactional
    @Override
    public boolean importRecipes(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            // 验证表头
            if (!validateHeaderRow(sheet.getRow(0))) {
                throw new RuntimeException("Excel文件格式不正确，请使用导出的模板");
            }

            // 获取所有图片（仅处理XSSF格式）
            Map<Integer, byte[]> imageMap = new HashMap<>();
            if (workbook instanceof XSSFWorkbook) {
                imageMap = extractImagesFromSheet((XSSFSheet) sheet);
            }

            List<Recipe> recipes = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Recipe recipe = buildRecipeFromRow(row);
                validateRecipe(recipe, i + 1);

                // 处理图片 - 优先使用嵌入图片，其次使用URL
                byte[] imageData = imageMap.get(i);
                if (imageData != null) {
                    String imageUrl = saveImportedImage(imageData, recipe.getName());
                    recipe.setImageUrl(imageUrl);
                    log.debug("行{}: 成功导入嵌入图片 -> {}");
                } else if (StringUtils.hasText(recipe.getImageUrl())) {
                    log.debug("行{}: 使用图片URL -> {}");
                }

                recipes.add(recipe);
            }

            // 批量保存
            saveRecipes(recipes);
            return true;
        } catch (Exception e) {
            log.error("导入食谱失败", e);
            throw new RuntimeException("导入失败: " + e.getMessage());
        }
    }

    // 验证表头行
    private boolean validateHeaderRow(Row headerRow) {
        if (headerRow == null || headerRow.getPhysicalNumberOfCells() < 8) {
            return false;
        }
        // 可以添加更详细的列名验证
        return true;
    }

    // 从sheet中提取所有图片并映射到行号
    private Map<Integer, byte[]> extractImagesFromSheet(XSSFSheet sheet) {
        Map<Integer, byte[]> imageMap = new HashMap<>();
        XSSFDrawing drawing = sheet.getDrawingPatriarch();

        if (drawing != null) {
            for (XSSFShape shape : drawing.getShapes()) {
                if (shape instanceof XSSFPicture) {
                    XSSFPicture picture = (XSSFPicture) shape;
                    XSSFClientAnchor anchor = picture.getClientAnchor();

                    // 获取图片所在行（注意: getRow1()返回的是0-based行号）
                    int rowNum = anchor.getRow1();
                    byte[] imageData = picture.getPictureData().getData();

                    imageMap.put(rowNum, imageData);
//                    log.debug("发现图片在行{}，大小: {}字节", rowNum, imageData.length);
                }
            }
        }
        return imageMap;
    }

    // 从行数据构建Recipe对象
    private Recipe buildRecipeFromRow(Row row) {
        Recipe recipe = new Recipe();
        recipe.setId(getCellIntValue(row.getCell(0)));
        recipe.setName(getCellStringValue(row.getCell(1)));
        recipe.setMealType(getCellStringValue(row.getCell(2)));
        recipe.setIngredients(getCellStringValue(row.getCell(3)));
        recipe.setSteps(getCellStringValue(row.getCell(4)));
        recipe.setNutritionInfo(getCellStringValue(row.getCell(5)));
        recipe.setImageUrl(getCellStringValue(row.getCell(6)));
        return recipe;
    }

    // 验证食谱数据
    private void validateRecipe(Recipe recipe, int rowNumber) {
        if (recipe.getName() == null || recipe.getName().trim().isEmpty()) {
            throw new RuntimeException("第" + rowNumber + "行: 食谱名称不能为空");
        }

        if (recipe.getMealType() == null ||
                !Arrays.asList("早餐", "午餐", "晚餐").contains(recipe.getMealType())) {
            throw new RuntimeException("第" + rowNumber + "行: 餐点类型必须为'早餐'、'午餐'或'晚餐'");
        }

        // 设置默认值
        if (recipe.getIngredients() == null || recipe.getIngredients().trim().isEmpty()) {
            recipe.setIngredients("未提供食材");
        }

        if (recipe.getSteps() == null || recipe.getSteps().trim().isEmpty()) {
            recipe.setSteps("未提供制作步骤");
        }
    }

    // 保存图片到服务器
    private String saveImportedImage(byte[] imageData, String prefix) throws IOException {
        String filename = "recipe_" + System.currentTimeMillis() + "_" +
                UUID.randomUUID().toString().substring(0, 8) + ".jpg";
        Files.write(Paths.get(uploadDir, filename), imageData);

        // 返回完整URL
        return "http://localhost:8080" + accessPath + filename; // 或用配置的域名
    }

    // 根据图片内容确定扩展名
    private String determineImageExtension(byte[] imageData) {
        if (imageData.length >= 8) {
            // PNG检查
            if (imageData[0] == (byte) 0x89 && imageData[1] == (byte) 0x50 &&
                    imageData[2] == (byte) 0x4E && imageData[3] == (byte) 0x47) {
                return ".png";
            }
            // GIF检查
            if (imageData[0] == (byte) 0x47 && imageData[1] == (byte) 0x49 &&
                    imageData[2] == (byte) 0x46) {
                return ".gif";
            }
            // JPEG检查
            if (imageData[0] == (byte) 0xFF && imageData[1] == (byte) 0xD8) {
                return ".jpg";
            }
        }
        return ".jpg"; // 默认
    }

    // 批量保存食谱
    private void saveRecipes(List<Recipe> recipes) {
        for (Recipe recipe : recipes) {
            QueryWrapper<Recipe> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name", recipe.getName());
            Recipe existing = this.baseMapper.selectOne(queryWrapper);

            if (existing == null) {
                this.baseMapper.insert(recipe);
                //log.debug("新增食谱: {}", recipe.getName());
            } else {
                recipe.setId(existing.getId());
                this.baseMapper.updateById(recipe);
                //log.debug("更新食谱: {}", recipe.getName());
            }
        }
    }

    // 辅助方法：获取单元格字符串值
    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    // 辅助方法：获取单元格整数值
    private Integer getCellIntValue(Cell cell) {
        if (cell == null) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                return Integer.parseInt(cell.getStringCellValue());
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }



    @Override
    public void exportRecipeData(HttpServletResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("食谱数据");

            // 设置列宽
            sheet.setColumnWidth(0, 10 * 256);    // ID列
            sheet.setColumnWidth(1, 20 * 256);    // 食谱名称列
            sheet.setColumnWidth(2, 15 * 256);    // 餐点类型列
            sheet.setColumnWidth(3, 30 * 256);    // 食材清单列
            sheet.setColumnWidth(4, 40 * 256);    // 制作步骤列
            sheet.setColumnWidth(5, 25 * 256);    // 营养信息列
            sheet.setColumnWidth(6, 20 * 256);    // 图片列

            // 添加表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("食谱名称");
            headerRow.createCell(2).setCellValue("餐点类型");
            headerRow.createCell(3).setCellValue("食材清单");
            headerRow.createCell(4).setCellValue("制作步骤");
            headerRow.createCell(5).setCellValue("营养信息");
            headerRow.createCell(6).setCellValue("图片");

            // 填充数据
            List<Recipe> recipes = list();
            int rowNum = 1;
            for (Recipe recipe : recipes) {
                Row row = sheet.createRow(rowNum);
                row.createCell(0).setCellValue(recipe.getId());
                row.createCell(1).setCellValue(recipe.getName());
                row.createCell(2).setCellValue(recipe.getMealType());
                row.createCell(3).setCellValue(recipe.getIngredients());
                row.createCell(4).setCellValue(recipe.getSteps());
                row.createCell(5).setCellValue(recipe.getNutritionInfo());

                // 嵌入图片
                if (StringUtils.hasText(recipe.getImageUrl())) {
                    try {
                        byte[] imageBytes = downloadImage(recipe.getImageUrl());
                        int pictureIdx = workbook.addPicture(imageBytes, getImageType(recipe.getImageUrl()));

                        CreationHelper helper = workbook.getCreationHelper();
                        Drawing<?> drawing = sheet.createDrawingPatriarch();

                        ClientAnchor anchor = helper.createClientAnchor();
                        anchor.setCol1(6); // 图片放在第8列
                        anchor.setRow1(rowNum); // 与当前行对齐
                        anchor.setCol2(7); // 结束列
                        anchor.setRow2(rowNum + 1); // 结束行

                        Picture pict = drawing.createPicture(anchor, pictureIdx);

                        // 调整图片大小 - 使用新API
                        if (pict instanceof XSSFPicture) {
                            XSSFPicture xssfPicture = (XSSFPicture) pict;
                            xssfPicture.getCTPicture().getSpPr().getXfrm().getExt().setCx(1000000); // 宽度
                            xssfPicture.getCTPicture().getSpPr().getXfrm().getExt().setCy(1000000); // 高度
                        }

                        // 调整行高以适应图片
                        row.setHeightInPoints(80);
                    } catch (Exception e) {
                        log.error("图片嵌入失败: {}"+ recipe.getImageUrl(), e);
                        row.createCell(7).setCellValue("[图片加载失败]");
                    }
                }
                rowNum++;
            }

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=recipes_" + System.currentTimeMillis() + ".xlsx");
            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            throw new RuntimeException("导出失败", e);
        }
    }

    private byte[] downloadImage(String imageUrl) throws IOException {
        // 如果是本地路径，直接读取文件
        if (imageUrl.startsWith("/recipe-images/") || imageUrl.startsWith("/images/")) {
            String filePath = imageUrl.replaceFirst("/recipe-images/", "").replaceFirst("/images/", "");
            Path imagePath = Paths.get(uploadDir, filePath);
            return Files.readAllBytes(imagePath);
        }

        // 如果是网络URL，使用HTTP下载
        try (InputStream in = new URL(imageUrl).openStream()) {
            return IOUtils.toByteArray(in);
        }
    }

    private int getImageType(String imageUrl) {
        if (imageUrl.toLowerCase().endsWith(".png")) {
            return Workbook.PICTURE_TYPE_PNG;
        } else {
            return Workbook.PICTURE_TYPE_JPEG;
        }
    }
}