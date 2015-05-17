test: test-u test-s

test-u:
	sbt test

run-l:
	docker run -p 8888:8888 --rm -it coinsignals/signals

build:
	sbt assembly
	docker build -t coinsignals/signals docker/

deploy-s:
	cd docker; eb use signals-staging; eb deploy;

test-s-s:

deploy-p:
	cd docker; eb use signals; eb deploy;