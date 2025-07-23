# Makefile pour lancer tous les services avec une seule commande

up:
	docker-compose up --build

down:
	docker-compose down

restart:
	make down
	make up

logs:
	docker-compose logs -f 