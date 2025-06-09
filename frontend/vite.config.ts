// frontend/vite.config.ts
import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
	plugins: [sveltekit()],

	resolve: {
		alias: {
			'$lib': './src/lib'
		}
	},

	server: {
		port: 5173,
		proxy: {
			// API 요청을 Spring Boot로 프록시
			'/api': {
				target: 'http://localhost:8080',
				changeOrigin: true,
				secure: false
			},
			// 기존 정적 리소스도 프록시 (CSS, 이미지 등)
			'/css': {
				target: 'http://localhost:8080',
				changeOrigin: true
			},
			'/images': {
				target: 'http://localhost:8080',
				changeOrigin: true
			}
		}
	}
});