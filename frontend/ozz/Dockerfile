# 1. 빌드 단계
FROM node:20-alpine AS builder

# 2. 작업 디렉터리 설정
WORKDIR /app/frontend/ozz

# 3. 패키지 파일 복사 및 의존성 설치
COPY frontend/ozz/package.json frontend/ozz/package-lock.json ./
RUN mkdir -p src/lib
COPY frontend/ozz/src/lib/sjw-project-demo-header-0.1.2.tgz ./src/lib/sjw-project-demo-header-0.1.2.tgz
RUN npm install

# 4. 애플리케이션 소스 복사
COPY frontend/ozz ./

# 5. Next.js 애플리케이션 빌드
RUN npm run build

# 6. 런타임 단계
FROM node:20-alpine AS runner

# 7. 작업 디렉터리 설정
WORKDIR /app

# 8. 빌드된 파일 복사
COPY --from=builder /app/frontend/ozz/.next ./.next
COPY --from=builder /app/frontend/ozz/node_modules ./node_modules
COPY --from=builder /app/frontend/ozz/public ./public
COPY --from=builder /app/frontend/ozz/next.config.mjs ./next.config.mjs
COPY --from=builder /app/frontend/ozz/package.json ./package.json

# 10. 포트 노출
EXPOSE 3000

# 11. Next.js 애플리케이션 실행
CMD ["npm", "start"]
