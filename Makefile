# 개발 DB 규칙 변경
deploy-dev:
	firebase deploy --project dmu-delibuddy-dev --only firestore:rules,storage

# 배포 DB 규칙 변경
deploy-prod:
	firebase deploy --project dmu-delibuddy --only firestore:rules,storage
