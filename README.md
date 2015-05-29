# Signals Microservice
Microservice used in the coinsignals project.

## Interface
	POST: /streams/'streamID'/signals 	{'-1,0 or 1'}
	GET:  /streams/'streamID'/signals? (optional URL-params: fromId, toId, afterTime, beforeTime and lastN (see combinations at the bottom of this page))
	GET:  /streams/'streamID'/status

## OSX Set Up
##### Docker
Local if when runing docker: localhost

One time setup:

	boot2docker init
	VBoxManage modifyvm "boot2docker-vm" --natpf1 "postgres-port,tcp,127.0.0.1,5432,,5432" #osx specific bind (local) # set postgres to "listen on *" and "host all all 0.0.0.0/0 trust"

Setup on each shell:

	boot2docker start
	eval "$(boot2docker shellinit)"

##### Deployment
One time setup:
	
	cd docker
	eb init (then select environment etc...)

## Makefile
	-test 
	-test-u (unit)
	-test-s (service)
	-run-l (run local)
	-build (builds a artifect and place it in the docker folder and afther that build the docker container)
	-deploy-s (deploy on staging)
	-test-s-s (service tests ageins staging)
	-deploy-p (deploy in production)
	
## Valid GET requests parameter combinations:
	fromId,   toId      	=>  alle signaler mellom 			(not including)
	afterTime, beforeTime   =>  alle signaler mellom 			(not including)
	afterId              	=>  alle signaler fra id og opp 	(not including)
	toId                	=>  alle signaler før id'en 		(not including)
	afterTime            	=>  alle signaler fra tid til nå 	(not including)
	beforeTime              =>  alle signaler før tidspunktet 	(not including)
	lastN		        	=>  returnerer siste n signaler