# Retour d’expérience – Projet Rattrapage

## Présentation générale
Le projet Rattrapage vise à offrir une plateforme moderne de gestion de films, avec une forte exigence sur l’accessibilité (RGAA), la sécurité et la modularité. Il combine un backend Spring Boot et un frontend Angular.

## Choix techniques
- **Frontend** : Angular (modularité, réactivité, écosystème riche)
- **Backend** : Spring Boot (sécurité, robustesse, REST API)
- **Base de données** : H2 (dev), extensible à PostgreSQL/MySQL
- **API externe** : TMDB pour la recherche et les métadonnées films
- **Export** : Apache POI (Excel), jsPDF/html2canvas (PDF)
- **Sécurité** : JWT, rôles, endpoints protégés

## Organisation du code
- Séparation stricte frontend/backend
- Composants Angular réutilisables, services dédiés
- Backend structuré en contrôleurs, services, entités, sécurité
- Utilisation de DTO côté API pour découpler la base

## Points forts
- **Accessibilité** : page dédiée, contraste, taille de police, alternatives visuelles, navigation clavier
- **Interface moderne** : responsive, animations, filtres dynamiques, graphiques interactifs
- **Export avancé** : Excel multi-feuilles, PDF des statistiques
- **Sécurité** : authentification JWT, rôles, endpoints protégés
- **Modularité** : code organisé, facile à maintenir et à étendre

## Points faibles / Limites
- Pas de gestion avancée des droits (ex : partage public limité)
- Pas de notifications temps réel (WebSocket)
- Export PDF limité à la section visible (pas de multi-pages)
- Pas de déploiement HTTPS par défaut (dev)

## Difficultés rencontrées
- Gestion des dépendances npm (conflits peer-deps)
- Mapping des genres TMDB (structure variable)
- Export PDF : rendu fidèle des graphiques, gestion du responsive
- Sécurité : gestion des tokens et des rôles côté Angular

## Axes d’amélioration
- Ajouter des tests unitaires et d’intégration (frontend)
- Permettre le partage public de statistiques (lien temporaire)
- Améliorer l’export PDF (multi-pages, personnalisation)
- Déployer en HTTPS avec un reverse proxy (Nginx, Caddy…)
- Ajouter des notifications (films ajoutés, rappels)
- Internationalisation (anglais, espagnol…)

## Bilan personnel
Ce projet m’a permis de consolider mes compétences fullstack, d’approfondir l’accessibilité web (RGAA) et de travailler sur l’export de données. L’organisation modulaire et la documentation facilitent la prise en main et l’évolution du code. Les choix techniques se sont révélés pertinents pour un projet de cette ampleur. 