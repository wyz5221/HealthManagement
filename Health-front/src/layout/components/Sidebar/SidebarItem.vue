<template>
  <div v-if="!item.hidden">
    <!-- 情况1：需要显示为下拉菜单的项（有多个子菜单） -->
    <el-submenu
      v-if="hasMultipleChildren"
      :index="resolvePath(item.path)"
      :popper-append-to-body="false"
    >
      <template #title>
        <item :icon="item.meta.icon" :title="item.meta.title" />
      </template>
      <!-- 递归渲染子菜单 -->
      <sidebar-item
        v-for="child in item.children"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(item.path)"
      />
    </el-submenu>

    <!-- 情况2：单菜单项（如首页）或单子菜单项 -->
    <template v-else>
      <app-link :to="finalPath">
        <el-menu-item :index="finalPath">
          <item :icon="finalIcon" :title="finalTitle" />
        </el-menu-item>
      </app-link>
    </template>
  </div>
</template>

<script>
import path from 'path'
import { isExternal } from '@/utils/validate'
import Item from './Item'
import AppLink from './Link'

export default {
  name: 'SidebarItem',
  components: { Item, AppLink },
  props: {
    item: {
      type: Object,
      required: true
    },
    isNest: {
      type: Boolean,
      default: false
    },
    basePath: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      onlyOneChild: null
    }
  },
  computed: {
    // 判断是否需要显示为下拉菜单
    hasMultipleChildren() {
      const showingChildren = this.item.children?.filter(child => !child.hidden) || []
      return showingChildren.length > 1 || (this.item.alwaysShow && showingChildren.length > 0)
    },
    // 最终显示的路径
    finalPath() {
      return this.resolvePath(
        this.onlyOneChild?.path || this.item.path
      )
    },
    // 最终显示的图标
    finalIcon() {
      return this.onlyOneChild?.meta?.icon || this.item.meta?.icon
    },
    // 最终显示的标题
    finalTitle() {
      return this.onlyOneChild?.meta?.title || this.item.meta?.title
    }
  },
  created() {
    // 初始化时处理单子菜单情况
    if (this.item.children) {
      this.hasOneShowingChild(this.item.children, this.item)
    }
  },
  methods: {
    // 处理单子菜单情况（保留原逻辑）
    hasOneShowingChild(children = [], parent) {
      const showingChildren = children.filter(item => {
        if (item.hidden) {
          return false
        } else {
          this.onlyOneChild = item
          return true
        }
      })

      if (showingChildren.length === 1) {
        return true
      }

      if (showingChildren.length === 0) {
        this.onlyOneChild = { ...parent, path: '', noShowingChildren: true }
        return true
      }

      return false
    },
    // 路径解析方法
    resolvePath(routePath) {
      if (isExternal(routePath)) {
        return routePath
      }
      if (isExternal(this.basePath)) {
        return this.basePath
      }
      return path.resolve(this.basePath, routePath)
    }
  }
}
</script>

<style lang="scss" scoped>
.el-menu-item {
  &:focus,
  &:hover {
    outline: none;
    background-color: rgba(255, 255, 255, 0.1) !important;
  }
}
</style>
