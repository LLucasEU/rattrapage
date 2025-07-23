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

## Installation

### Prérequis
- Node.js >= 16
- Java 17
- Maven

### Backend (Spring Boot)
```bash
cd server
./mvnw spring-boot:run
```

### Frontend (Angular)
```bash
cd client
npm install
npm start
```

L’application sera accessible sur http://localhost:4200

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

## Contribution
Pull requests bienvenues. Merci de respecter la structure modulaire et les bonnes pratiques RGAA.

## Licence
MIT
