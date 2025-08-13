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

// 本地自托管 TinyMCE（必须已将 node_modules/tinymce 复制到 public/tinymce）
const scriptSrc = '/tinymce/tinymce.min.js'

const editorInit = {
	// 同意 GPL 许可（自托管开源版必须明确）
	license_key: 'gpl',

	// 尺寸
	min_height: props.minHeight,
	autoresize_bottom_margin: 30,

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

	// 外观（本地皮肤与内容样式）
	branding: false,
	promotion: false,
	skin_url: '/tinymce/skins/ui/oxide',
	content_css: '/tinymce/skins/content/default/content.min.css',

	// URL 不转换，避免破坏服务端返回路径
	convert_urls: false,

	// 图片上传（Promise 形式，兼容 v7+）
	automatic_uploads: true,
	images_upload_handler: (blobInfo, progress) => new Promise(async (resolve, reject) => {
		try {
			const formData = new FormData()
			formData.append('file', blobInfo.blob(), blobInfo.filename())
			const res = await service.post('/api/file/rte-upload', formData, {
				headers: { 'Content-Type': 'multipart/form-data' },
				onUploadProgress: (e) => {
					if (progress && e.total) progress(Math.round((e.loaded / e.total) * 100))
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

