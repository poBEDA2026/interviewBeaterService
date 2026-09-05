.PHONY: help build up down restart start logs ps clean test build-local

# Default target
help:
	@echo "InterviewBeaterService - Docker Commands"
	@echo ""
	@echo "Available targets:"
	@echo "  make build          - Build Docker images"
	@echo "  make up             - Start all services"
	@echo "  make down           - Stop all services"
	@echo "  make restart        - Restart all services"
	@echo "  make start          - Build and start all services"
	@echo "  make logs           - Show logs from all services"
	@echo "  make ps             - Show running containers"
	@echo "  make clean          - Remove all containers, volumes, and images"
	@echo "  make test           - Run tests in Docker"
	@echo "  make build-local    - Build JAR locally (without Docker)"

# Build Docker images
build:
	docker-compose -f ci/docker-compose.yml build --no-cache

# Start all services
up:
	docker-compose -f ci/docker-compose.yml up -d

# Stop all services
down:
	docker-compose -f ci/docker-compose.yml down

# Restart all services (rebuilds and restarts)
restart: build down up

# Build and start all services
start: build up

# Show logs
logs:
	docker-compose -f ci/docker-compose.yml logs -f $(c)

# Show running containers
ps:
	docker-compose -f ci/docker-compose.yml ps

# Clean everything
clean:
	docker-compose -f ci/docker-compose.yml down -v --remove-orphans
	docker system prune -f
	@echo "All containers, volumes, and unused images removed"

# Run tests
test:
	docker-compose -f ci/docker-compose.yml run --rm app mvn test

# Build locally
build-local:
	./mvnw clean package -DskipTests