<template>
	<Editor
		v-model="localValue"
		:init="editorInit"
		:tinymce-script-src="scriptSrc"
		style="width: 100%"
	/>
</template>

<script setup>
import { computed } from 'vue'
import Editor from '@tinymce/tinymce-vue'
import service from '@/utils/request'

const props = defineProps({
	modelValue: { type: String, default: '' },
	minHeight: { type: Number, default: 600 }
})
const emit = defineEmits(['update:modelValue'])

const localValue = computed({
	get: () => props.modelValue,
	set: (v) => emit('update:modelValue', v)
})

// 使用 Tiny Cloud（包含 API Key）加载脚本，避免本地静态资源缺失问题
// 注：该 Key 由用户提供，仅用于本项目
const scriptSrc = 'https://cdn.tiny.cloud/1/ab5gj0y1r4sibjfmoaeuex7jftfu99wi8xkyd7fy6l1yg9ri/tinymce/7/tinymce.min.js'

const editorInit = {
	// 尺寸
	min_height: props.minHeight,
	autoresize_bottom_margin: 30,

	// 云端版本通过脚本中的 API Key 授权，无需额外 license_key

	// 菜单/工具栏/插件
	menubar: 'file edit view insert format table',
	plugins: [
		'advlist', 'autolink', 'lists', 'link', 'image', 'charmap', 'preview', 'anchor',
		'searchreplace', 'visualblocks', 'fullscreen', 'insertdatetime', 'media', 'table', 'code', 'help', 'wordcount', 'autoresize'
	].join(' '),
	toolbar: [
		'undo redo | blocks | bold italic underline forecolor backcolor | alignleft aligncenter alignright alignjustify',
		'| bullist numlist outdent indent | link image table | removeformat | code preview'
	].join(' '),

	// 外观
	branding: false,
	promotion: false,

	// URL 不转换，避免破坏服务端返回路径
	convert_urls: false,

	// 图片上传（走后端接口，使用 Promise 规范的处理函数，兼容 TinyMCE v7+）
	automatic_uploads: true,
	images_upload_handler: (blobInfo, progress) => new Promise(async (resolve, reject) => {
		try {
			const formData = new FormData()
			formData.append('file', blobInfo.blob(), blobInfo.filename())
			const res = await service.post('/api/file/rte-upload', formData, {
				headers: { 'Content-Type': 'multipart/form-data' },
				onUploadProgress: (e) => {
					if (progress && e.total) {
						progress(Math.round((e.loaded / e.total) * 100))
					}
				},
				hideLoading: true
			})
			const data = res.data
			const url = data?.location || data?.data?.location || (data?.success && data.data) || data?.data
			if (!url || typeof url !== 'string') return reject('上传失败：无效返回')
			resolve(url)
		} catch (e) {
			reject('上传异常')
		}
	})
}
</script>

<style scoped>
:deep(.tox-tinymce){ width: 100% !important; }
</style>

