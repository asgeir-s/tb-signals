# Signals Microservice
Microservice used in the tradersbit project.

This requires Java Cryptography Extension (JCE) Unlimited Strength to run.
Error: ava.security.InvalidKeyException: Illegal key size or default parameters means JCE is missing.

## Interface
	POST: /streams/'streamID'/signals 	{'-1,0 or 1'} => the signal
	GET:  /streams/'streamID'/signals? (optional URL-params: onlyClosed, fromId, toId, afterTime, beforeTime and lastN (see combinations at the bottom of this page)) => all signals
	GET:  /streams/'streamID'/status => last signal
	
## Environment Variables
	AWS_ACCESS_KEY_ID (default 'none')
	AWS_SECRET_KEY (default 'none')
	AWS_SNS_REGION (default 'none')

    MICROSERVICES_HTTPS
	STREAMS_SERVICE_ADDRESS (default 'none')
	STREAMS_SERVICE_PORT (default '80')
	SERVICE_APIKEY (default 'none')

	LOG_LEVEL (default 'DEBUG')
	
	optional(initialized for testing and automatically provided by AWS):
	RDS_HOSTNAME
	RDS_PORT
	RDS_DB_NAME
	RDS_USERNAME
	RDS_PASSWORD_NEW (OBS: must be set!)
	
### Test parameters
    MICROSERVICES_HTTPS=false
    SERVICE_APIKEY=secret
	AWS_SNS_REGION=us-west-2
	LOG_LEVEL=DEBUG
	AWS_SECRET_KEY=q9/igUKZHmIarMVA8pjGkkd33/XTqHcjXZicO8Kp
	AWS_ACCESS_KEY_ID=AKIAJTDJWSYFWGUK5YOQ
	STREAMS_SERVICE_ADDRESS=tb-staging-streams.elasticbeanstalk.com

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
	onlyClosed		=>  alle lukkede handler (the last signal is CLOSE)
	fromId,   toId      	=>  alle signaler mellom 			(not including)
	afterTime, beforeTime   =>  alle signaler mellom 			(not including)
	afterId              	=>  alle signaler fra id og opp 	(not including)
	toId                	=>  alle signaler før id'en 		(not including)
	afterTime            	=>  alle signaler fra tid til nå 	(not including)
	beforeTime              =>  alle signaler før tidspunktet 	(not including)
	lastN		        	=>  returnerer siste n signaler


## Local Testing Setup

#### Postgres

postgresql.conf

    listen_addresses = '*'

pg_hba.conf

    # TYPE  DATABASE        USER            ADDRESS                 METHOD

    # "local" is for Unix domain socket connections only
    local   all             all                                     trust
    # IPv4 local connections:
    host    all             all             127.0.0.1/32            trust
    # IPv6 local connections:
    host    all             all             ::1/128                 trust
    host     all             all             0.0.0.0/0                 trust
    # Allow replication connections from localhost, by a user with the
    # replication privilege.
    #local   replication     sogasg                                trust
    #host    replication     sogasg        127.0.0.1/32            trust
    #host    replication     sogasg        ::1/128                 trust

With problems: RESTART COMPUTER :P
