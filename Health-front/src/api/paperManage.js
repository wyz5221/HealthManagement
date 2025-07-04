import request from '@/utils/request'

export function getPaperList() {
  return request({ url: '/paper/list', method: 'get' })
}

export function addPaper(data) {
  return request({ url: '/paper/add', method: 'post', data })
}

export function changePaperStatus(id, status) {
  return request({ url: `/paper/status/${id}?status=${status}`, method: 'post' })
}

export function deletePaper(id) {
  return request({ url: `/paper/delete/${id}`, method: 'post' })
}

export function addQuestions(paperId, questions) {
  return request({ url: `/paper/addQuestions/${paperId}`, method: 'post', data: questions })
}

export function getQuestions(paperId) {
  return request({ url: `/paper/questions/${paperId}`, method: 'get' })
} 