# 로컬 개발용 Elasticsearch 이미지
# 공식 이미지엔 한국어 형태소 분석기(nori)가 기본 포함 안 되어 있어서
# 빌드 시점에 플러그인을 설치해서 이미지를 하나 더 만들어 씀
FROM docker.elastic.co/elasticsearch/elasticsearch:8.11.1

RUN bin/elasticsearch-plugin install --batch analysis-nori
