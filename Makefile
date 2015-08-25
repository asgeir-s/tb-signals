test:
	sbt test

test-s:
	sbt "testOnly com.cluda.*Spec"

test-u:
	sbt "testOnly com.cluda.*Test"

run-l:
	docker run -p 8888:8888 -e "RDS_HOSTNAME=192.168.59.3" --rm -it coinsignals/signals

build:
	sbt assembly
	docker build -t coinsignals/signals docker/

deploy-s: build
	cd docker; eb deploy cs-signals-staging -r us-west-2;

test-s-s:

deploy-p: build
	cd docker; eb deploy cs-signals -r us-east-1;

setup-db:
	psql -p 5432 -c "create database coinsignals;"
	psql -p 5432 -c "CREATE USER testuser PASSWORD 'Password123';"
	psql -p 5432 -c "GRANT CONNECT ON DATABASE coinsignals TO testuser;"
	psql -p 5432 -d coinsignals -c "GRANT USAGE ON SCHEMA public to testuser;"
	psql -p 5432 -d coinsignals -c "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO testuser;"