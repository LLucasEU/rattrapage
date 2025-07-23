# Rattrapage

## Présentation
Rattrapage est une application web de gestion de films permettant à chaque utilisateur de suivre les films vus, à voir, d’obtenir des statistiques personnalisées, d’exporter ses listes et de profiter d’une interface accessible et moderne.

## Fonctionnalités principales
- Authentification sécurisée (utilisateur/admin)
- Recherche et ajout de films via l’API TMDB
- Gestion des listes : films à voir, films vus
- Statistiques graphiques (genres, périodes, etc.)
- Filtres dynamiques et interface responsive
- Export Excel et PDF des listes/statistiques
- Page d’accessibilité (contraste, taille de police, alternatives visuelles)
- Sécurité des accès et respect du RGAA

## Installation et lancement rapide

### Prérequis
- [Docker](https://www.docker.com/products/docker-desktop)
- [Docker Compose](https://docs.docker.com/compose/)
- (Optionnel) [Make](https://www.gnu.org/software/make/) pour simplifier les commandes

### Lancer toute l'application avec Make

À la racine du projet, il suffit d'exécuter :

```bash
make up
```

Cela va :
- Construire les images Docker du backend, du frontend et de la base de données
- Lancer tous les services (MySQL, API Spring Boot, Angular)

Pour arrêter tous les services :
```bash
make down
```

Pour redémarrer :
```bash
make restart
```

Pour voir les logs :
```bash
make logs
```

### Lancer manuellement avec Docker Compose

Si vous n'avez pas Make, vous pouvez utiliser directement Docker Compose :

```bash
docker-compose up --build
```

L'application sera accessible sur :
- Frontend : http://localhost:4200
- Backend (API) : http://localhost:8080
- MySQL : port 3306

## Structure du projet
```
rattrapage/
  client/    # Frontend Angular
  server/    # Backend Spring Boot
```
- **client/src/app/** : composants, services, modules Angular
- **server/src/main/java/com/nobass/server/** : contrôleurs, entités, services, sécurité

## Endpoints principaux (API)
- `POST /auth/login` : connexion
- `POST /auth/register` : inscription
- `GET /api/v1/movies/watchlist` : liste à voir
- `GET /api/v1/movies/watched` : films vus
- `GET /api/v1/movies/stats` : statistiques utilisateur
- `GET /api/v1/movies/export` : export Excel

## Accessibilité (RGAA)
- Contraste élevé, taille de police, alternatives visuelles
- Navigation clavier, focus visible, textes alternatifs
- Page dédiée dans le menu admin

## Export et partage
- Export Excel (2 feuilles : films vus, à voir)
- Export PDF des graphiques/statistiques

## Sécurité
- Authentification JWT, rôles utilisateur/admin
- Accès restreint aux données personnelles
- Prévu pour HTTPS en production
