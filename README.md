# Clinical Management Backend

Ce dépôt contient le backend du système de gestion clinique.

## Équipe de Développement

- **Jake Melvin TIOKOU**
- **Loïc Luc KENMOE MBEUKEM**
- **Jean Vincent YOUMSSI VINCENT**
- **Hassan Mahamat DOGO**
- **Belvinard POUADJEU**

## 🚀 Configuration du Projet

### Prérequis

- Java 17 ou supérieur
- Maven 3.6.3 ou supérieur
- Docker et Docker Compose
- PostgreSQL 13+

### Installation

1. **Cloner le dépôt** :
   ```bash
   git clone [URL_DU_DEPOT]
   cd clinical-management-backend
   ```

2. **Configurer les variables d'environnement** :
   ```bash
   cp env.template .env
   ```
   Puis éditez le fichier `.env` avec vos paramètres locaux.

3. **Démarrer les services requis** :
   ```bash
   # Démarrer PostgreSQL et MinIO
   docker compose up -d
   ```

4. **Construire et exécuter l'application** :
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Accéder à l'application** :
   - API : http://localhost:8080
   - Documentation Swagger : http://localhost:8080/swagger-ui.html
   - Console MinIO : http://localhost:9001 (identifiants dans .env)

### Configuration de l'environnement de développement

Pour configurer uniquement MinIO :
```bash
docker compose up minio --build -d
# ou si déjà construit :
docker compose up minio -d
```

### Commandes utiles

- **Lancer les tests** : `mvn test`
- **Construire le JAR** : `mvn clean package`
- **Lancer avec un profil spécifique** : `mvn spring-boot:run -Dspring-boot.run.profiles=dev`

configure .env variable to be recognise by your ide (intellij, vscode)

---

## 📚 Documentation Complète

- 📖 **[Documentation Technique et Fonctionnelle](DOCUMENTATION_TECHNIQUE_FONCTIONNELLE.md)** - Architecture, modèles de données, API complète
- 👤 **[Guide Utilisateur](GUIDE_UTILISATEUR.md)** - Manuel d'utilisation par rôle (Admin, Médecin, Secrétaire)
- 🚀 **[README Complet](README_ORIGINAL.md)** - Vue d'ensemble détaillée du projet

## 🏗️ Principes Clés

### Architecture Générale
- **Architecture REST API** : API RESTful avec Spring Boot
- **Sécurité JWT** : Authentification basée sur tokens JWT
- **Base de données relationnelle** : PostgreSQL avec JPA/Hibernate
- **Stockage de fichiers** : MinIO S3-compatible pour les documents
- **Communication temps réel** : WebSocket pour le chat et notifications
- **Documentation API** : Swagger/OpenAPI 3.0

### Validation Flow
- **Workflow de validation** : Défini par entité (Patient, Appointment, Invoice, Prescription) et type d'action
- **Étapes de validation** : Chaque opération suit des étapes de validation selon le rôle utilisateur
- **Groupes de validateurs** : Système de rôles hiérarchiques (ADMIN > DOCTOR > SECRETARY)
- **Restrictions** : Séparation des responsabilités - l'initiateur ne peut pas valider sa propre action

### Hiérarchie des Rôles
1. **ADMIN** : Accès complet, gestion des utilisateurs, validation de niveau supérieur
2. **DOCTOR** : Gestion médicale, prescriptions, validation des actes médicaux
3. **SECRETARY** : Gestion administrative, rendez-vous, patients

## 📊 Modèle de Données

### Entités Principales

#### 1. Sécurité et Utilisateurs
```java
// User - Utilisateurs du système
- id: Long (PK)
- username: String (unique)
- email: String (unique)
- password: String (encodé BCrypt)
- firstName: String
- lastName: String
- role: Role (ADMIN, DOCTOR, SECRETARY)

// Role - Énumération des rôles
- ADMIN: Administration complète
- DOCTOR: Personnel médical
- SECRETARY: Personnel administratif
```

#### 2. Gestion des Patients
```java
// Patient - Dossiers patients
- id: Long (PK)
- firstName: String
- lastName: String
- dateOfBirth: LocalDate
- gender: Gender (MALE, FEMALE, OTHER)
- address: String
- phoneNumber: String (unique)
- email: String (unique, optionnel)
- medicalHistory: Text
- allergies: Text
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

#### 3. Gestion des Rendez-vous
```java
// Appointment - Rendez-vous médicaux
- id: Long (PK)
- dateTime: LocalDateTime
- reason: String
- patient: Patient (FK)
- doctor: String
- room: String
- status: Status (SCHEDULED, CANCELLED, COMPLETED, LATE_CANCELLED, CLINIC_CANCELLED)
- cancellationInitiator: String
- cancellationReason: String
```

#### 4. Prescriptions Médicales
```java
// Prescription - Ordonnances
- id: Long (PK)
- diagnostic: Text
- recommandations: Text
- createdAt: LocalDateTime
- patient: Patient (FK)
- medecin: User (FK)
```

#### 5. Facturation
```java
// Invoice - Factures
- id: Long (PK)
- patient: Patient (FK)
- amount: BigDecimal
- issuedAt: LocalDateTime
- paid: Boolean
- datePaid: LocalDateTime
- description: Text
```

#### 6. Système de Notifications
```java
// Notification - Notifications système
- id: Long (PK)
- sender: User (FK)
- type: NotificationType
- channel: NotificationChannel
- subject: String
- content: Text
- status: NotificationStatus
- createdAt: LocalDateTime
- readAt: LocalDateTime

// UserNotification - Relation utilisateur-notification
- notification: Notification (FK)
- recipient: User (FK)
- status: NotificationStatus
```

#### 7. Chat et Communication
```java
// ChatMessage - Messages de chat
- content: String
- senderId: Long
- senderName: String
- recipientId: Long
- recipientName: String
- type: MessageType (JOIN, LEAVE, CHAT)

// ChatMessageEntity - Persistance des messages
- id: Long (PK)
- content: String
- senderId: Long
- senderName: String
- recipientId: Long
- recipientName: String
- type: MessageType
- timestamp: LocalDateTime
```

## 💻 Interfaces et API

### 1. Authentification (`/api/v1/auth`)
- `POST /register` : Inscription nouvel utilisateur
- `POST /login` : Connexion utilisateur

### 2. Gestion des Patients (`/api/v1/patients`)
- `GET /` : Liste tous les patients
- `GET /{id}` : Détails d'un patient
- `POST /` : Créer un patient
- `PUT /{id}` : Modifier un patient
- `DELETE /{id}` : Supprimer un patient

### 3. Gestion des Rendez-vous (`/api/v1/appointments`)
- `GET /` : Liste paginée avec filtres (docteur, date, salle, statut)
- `GET /{id}` : Détails d'un rendez-vous
- `POST /` : Créer un rendez-vous (SECRETARY)
- `PUT /{id}` : Modifier un rendez-vous (SECRETARY)
- `PUT /{id}/cancel` : Annuler un rendez-vous
- `PUT /{id}/complete` : Marquer comme terminé
- `DELETE /{id}` : Supprimer définitivement (ADMIN)
- `GET /statuses` : Statuts disponibles
- `GET /alternatives` : Créneaux alternatifs

### 4. Prescriptions (`/api/v1/prescriptions`)
- `GET /` : Liste des prescriptions
- `GET /{id}` : Détails d'une prescription
- `POST /` : Créer une prescription (DOCTOR)
- `PUT /{id}` : Modifier une prescription (DOCTOR)
- `DELETE /{id}` : Supprimer une prescription

### 5. Facturation (`/api/v1/invoices`)
- `GET /` : Liste des factures
- `GET /{id}` : Détails d'une facture
- `POST /` : Créer une facture
- `PUT /{id}` : Modifier une facture
- `PUT /{id}/pay` : Marquer comme payée
- `GET /{id}/pdf` : Générer PDF

### 6. Notifications (`/api/v1/notifications`)
- `GET /` : Notifications de l'utilisateur
- `POST /` : Créer une notification
- `PUT /{id}/read` : Marquer comme lue
- `PUT /archive` : Archiver des notifications

### 7. Chat (`/api/v1/chat`, WebSocket `/ws`)
- `GET /messages` : Historique des messages
- `POST /send` : Envoyer un message
- WebSocket endpoints pour communication temps réel

### 8. Administration (`/api/v1/admin`)
- `GET /users` : Liste des utilisateurs (ADMIN)
- `POST /users` : Créer un utilisateur (ADMIN)
- `PUT /users/{id}` : Modifier un utilisateur (ADMIN)
- `DELETE /users/{id}` : Supprimer un utilisateur (ADMIN)

### 9. Tableaux de Bord
- `/api/v1/dashboard/admin` : Statistiques administrateur
- `/api/v1/dashboard/doctor` : Statistiques médecin
- `/api/v1/dashboard/secretary` : Statistiques secrétaire

## 🧾 Règles de Décision et Validation

### Workflow de Validation des Rendez-vous
1. **Création** : Seuls les SECRETARY peuvent créer
2. **Modification** : Seuls les SECRETARY peuvent modifier
3. **Annulation** : SECRETARY et DOCTOR peuvent annuler
4. **Finalisation** : SECRETARY et DOCTOR peuvent marquer comme terminé
5. **Suppression** : Seuls les ADMIN peuvent supprimer définitivement

### Workflow de Validation des Prescriptions
1. **Création** : Seuls les DOCTOR peuvent créer
2. **Modification** : Seuls les DOCTOR peuvent modifier
3. **Suppression** : DOCTOR et ADMIN peuvent supprimer

### Workflow de Validation des Factures
1. **Création** : SECRETARY et ADMIN peuvent créer
2. **Modification** : SECRETARY et ADMIN peuvent modifier
3. **Paiement** : SECRETARY et ADMIN peuvent marquer comme payée

### Règles de Sécurité
- **JWT Token** : Expiration 24h, refresh token 7 jours
- **Chiffrement** : Mots de passe avec BCrypt
- **CORS** : Configuration pour domaines autorisés
- **Validation** : Validation des données avec Bean Validation
- **Audit** : Traçabilité des actions avec timestamps

## 🔑 Permissions et Autorisations

### Matrice des Permissions

| Fonctionnalité | ADMIN | DOCTOR | SECRETARY |
|----------------|-------|---------|-----------|
| Gestion Utilisateurs | ✅ | ❌ | ❌ |
| Gestion Patients | ✅ | ✅ | ✅ |
| Création Rendez-vous | ✅ | ❌ | ✅ |
| Modification Rendez-vous | ✅ | ❌ | ✅ |
| Annulation Rendez-vous | ✅ | ✅ | ✅ |
| Suppression Rendez-vous | ✅ | ❌ | ❌ |
| Création Prescriptions | ✅ | ✅ | ❌ |
| Gestion Factures | ✅ | ❌ | ✅ |
| Chat/Notifications | ✅ | ✅ | ✅ |
| Tableaux de Bord | ✅ | ✅ | ✅ |

### Permissions Techniques
- `VIEW` : Consultation des données
- `CREATE` : Création d'entités
- `UPDATE` : Modification d'entités
- `DELETE` : Suppression d'entités
- `MANAGE` : Gestion complète (ADMIN uniquement)

## 📌 User Stories

### Gestion des Utilisateurs (ADMIN)
- En tant qu'administrateur, je peux créer/modifier/supprimer des comptes utilisateurs
- En tant qu'administrateur, je peux assigner des rôles aux utilisateurs
- En tant qu'administrateur, je peux consulter les logs d'activité

### Gestion des Patients (ADMIN, DOCTOR, SECRETARY)
- En tant que personnel médical, je peux créer un dossier patient
- En tant que personnel médical, je peux consulter l'historique médical
- En tant que personnel médical, je peux mettre à jour les informations patient

### Gestion des Rendez-vous (SECRETARY)
- En tant que secrétaire, je peux planifier un rendez-vous
- En tant que secrétaire, je peux modifier/annuler un rendez-vous
- En tant que secrétaire, je peux consulter le planning médecin
- En tant que secrétaire, je peux trouver des créneaux alternatifs

### Gestion Médicale (DOCTOR)
- En tant que médecin, je peux créer une prescription
- En tant que médecin, je peux consulter les dossiers patients
- En tant que médecin, je peux marquer un rendez-vous comme terminé
- En tant que médecin, je peux annuler un rendez-vous

### Communication (ALL ROLES)
- En tant qu'utilisateur, je peux envoyer/recevoir des messages
- En tant qu'utilisateur, je peux recevoir des notifications
- En tant qu'utilisateur, je peux marquer les notifications comme lues

## 🛠️ Configuration Technique

### Technologies Utilisées
- **Backend** : Spring Boot 3.3.0, Java 17
- **Sécurité** : Spring Security, JWT (jjwt 0.12.5)
- **Base de données** : PostgreSQL, JPA/Hibernate
- **Documentation** : Swagger/OpenAPI 3.0
- **Stockage** : MinIO S3-compatible
- **Communication** : WebSocket, Spring Messaging
- **PDF** : iText PDF, Apache PDFBox
- **Build** : Maven
- **Conteneurisation** : Docker, Docker Compose

### Variables d'Environnement
```properties
# Base de données
DATABASE_URL=jdbc:postgresql://...
DATABASE_USERNAME=clinic_db_owner
DATABASE_PASSWORD=***

# Application
BACKEND_PORT=8888
JWT_SECRET_KEY=***
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200

# Stockage MinIO
MINIO_API_URL=***
MINIO_USERNAME=***
MINIO_PASSWORD=***
MINIO_PROJECT_NAME=clinical-management

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=***
MAIL_PASSWORD=***
```

### Déploiement
```bash
# Configuration
cp env.template .env

# Lancement MinIO
docker compose up minio -d

# Build et démarrage
mvn clean install
java -jar target/clinic-backend-0.0.1-SNAPSHOT.jar
```

## 📈 Statut de Développement

### Fonctionnalités Prêtes (v0.1.0 - 2025)
- ✅ Authentification JWT
- ✅ Gestion des utilisateurs et rôles
- ✅ CRUD Patients complet
- ✅ CRUD Rendez-vous avec workflow
- ✅ CRUD Prescriptions
- ✅ CRUD Factures avec génération PDF
- ✅ Système de notifications
- ✅ Chat temps réel WebSocket
- ✅ Tableaux de bord par rôle
- ✅ Documentation API Swagger
- ✅ Configuration Docker

### Fonctionnalités en Test
- 🧪 Intégration MinIO pour stockage fichiers
- 🧪 Notifications email automatiques
- 🧪 Rapports et statistiques avancées

### Fonctionnalités en Attente
- ⏳ Module de facturation avancée
- ⏳ Intégration calendrier externe
- ⏳ Module de télémédecine
- ⏳ API mobile dédiée

## 👉 Résumé

Le **Clinical Management Backend** est une solution complète de gestion hospitalière avec :

- **Architecture robuste** : Spring Boot, sécurité JWT, base PostgreSQL
- **Workflow de validation** : Système de rôles hiérarchiques avec séparation des responsabilités
- **Fonctionnalités complètes** : Gestion patients, rendez-vous, prescriptions, facturation
- **Communication intégrée** : Chat temps réel et notifications
- **Sécurité avancée** : Authentification, autorisation, audit trail
- **Documentation complète** : API Swagger, guides d'installation
- **Prêt pour production** : Configuration Docker, variables d'environnement

Le système respecte les bonnes pratiques médicales avec traçabilité complète des actions et validation multi-niveaux pour garantir la qualité et la sécurité des soins.



# Guide Utilisateur - Clinical Management System

## 🏥 Bienvenue dans le Système de Gestion Clinique

Ce guide vous accompagne dans l'utilisation du système de gestion clinique selon votre rôle : **Administrateur**, **Médecin** ou **Secrétaire**.

## 🚀 Premiers Pas

### Connexion au Système
1. Accédez à l'application via votre navigateur
2. Utilisez vos identifiants fournis par l'administrateur
3. Votre tableau de bord s'affiche selon votre rôle

### Comptes par Défaut (Environnement de Test)
- **Administrateur** : admin@belvicare.com / password
- **Médecin** : doctor@belvicare.com / password  
- **Secrétaire** : secretary@belvicare.com / password

## 👨‍💼 Guide Administrateur

### Tableau de Bord Administrateur
Votre tableau de bord affiche :
- Nombre total d'utilisateurs, patients, rendez-vous
- Statistiques des revenus et factures
- Graphiques d'activité mensuelle
- Alertes système importantes

### Gestion des Utilisateurs
**Créer un nouvel utilisateur :**
1. Menu "Gestion Utilisateurs" → "Nouveau"
2. Remplissez les informations :
   - Nom d'utilisateur (unique)
   - Email (unique)
   - Mot de passe temporaire
   - Prénom et nom
   - Rôle (Admin/Médecin/Secrétaire)
3. Cliquez "Créer"
4. L'utilisateur recevra ses identifiants par email

**Modifier un utilisateur :**
1. Liste des utilisateurs → Cliquez sur l'utilisateur
2. Modifiez les informations nécessaires
3. Sauvegardez les modifications

**Supprimer un utilisateur :**
1. Liste des utilisateurs → Actions → Supprimer
2. Confirmez la suppression (action irréversible)

### Supervision Générale
- **Patients** : Accès complet à tous les dossiers
- **Rendez-vous** : Vue globale du planning
- **Factures** : Gestion complète de la facturation
- **Rapports** : Génération de rapports d'activité

### Paramètres Système
- Configuration des notifications
- Gestion des sauvegardes
- Paramètres de sécurité
- Maintenance système

## 👨‍⚕️ Guide Médecin

### Tableau de Bord Médecin
Votre tableau de bord affiche :
- Vos rendez-vous du jour
- Patients récents consultés
- Prescriptions en attente
- Messages et notifications

### Gestion des Patients
**Consulter un dossier patient :**
1. Menu "Patients" → Rechercher ou sélectionner
2. Consultez l'historique médical complet
3. Vérifiez les allergies et traitements en cours

**Mettre à jour un dossier :**
1. Ouvrez le dossier patient
2. Ajoutez des notes médicales
3. Mettez à jour l'historique médical
4. Sauvegardez les modifications

### Gestion des Rendez-vous
**Consulter vos rendez-vous :**
1. Menu "Planning" → Vue par jour/semaine/mois
2. Filtrez par statut (Programmé/Terminé/Annulé)
3. Cliquez sur un rendez-vous pour les détails

**Marquer un rendez-vous comme terminé :**
1. Sélectionnez le rendez-vous
2. Cliquez "Marquer terminé"
3. Ajoutez des notes si nécessaire

**Annuler un rendez-vous :**
1. Sélectionnez le rendez-vous
2. Cliquez "Annuler"
3. Indiquez la raison de l'annulation
4. Le patient sera automatiquement notifié

### Prescriptions Médicales
**Créer une prescription :**
1. Menu "Prescriptions" → "Nouvelle"
2. Sélectionnez le patient
3. Saisissez le diagnostic
4. Ajoutez les recommandations/traitements
5. Sauvegardez et imprimez si nécessaire

**Modifier une prescription :**
1. Liste des prescriptions → Sélectionnez
2. Modifiez le diagnostic ou les recommandations
3. Sauvegardez les modifications

### Communication
**Chat interne :**
1. Icône chat en bas à droite
2. Sélectionnez un collègue
3. Échangez en temps réel
4. L'historique est conservé

**Notifications :**
- Nouveaux rendez-vous assignés
- Modifications de planning
- Messages urgents
- Rappels de tâches

## 👩‍💼 Guide Secrétaire

### Tableau de Bord Secrétaire
Votre tableau de bord affiche :
- Rendez-vous du jour
- Patients en attente
- Factures impayées
- Tâches administratives

### Gestion des Patients
**Créer un nouveau patient :**
1. Menu "Patients" → "Nouveau"
2. Remplissez les informations personnelles :
   - Nom, prénom, date de naissance
   - Adresse, téléphone, email
   - Genre et informations médicales
3. Sauvegardez le dossier

**Rechercher un patient :**
1. Utilisez la barre de recherche
2. Filtrez par nom, téléphone ou email
3. Cliquez sur le patient pour ouvrir son dossier

### Gestion des Rendez-vous
**Planifier un rendez-vous :**
1. Menu "Rendez-vous" → "Nouveau"
2. Sélectionnez le patient
3. Choisissez le médecin et la date/heure
4. Indiquez la raison de la consultation
5. Assignez une salle
6. Confirmez la création

**Modifier un rendez-vous :**
1. Sélectionnez le rendez-vous dans le planning
2. Modifiez les informations nécessaires
3. Sauvegardez les modifications
4. Les parties concernées sont notifiées

**Gérer les annulations :**
1. Sélectionnez le rendez-vous à annuler
2. Choisissez le motif d'annulation
3. Proposez des créneaux alternatifs si possible
4. Confirmez l'annulation

**Trouver des créneaux alternatifs :**
1. Fonction "Créneaux alternatifs"
2. Sélectionnez le médecin et la période souhaitée
3. Le système propose des créneaux libres
4. Proposez au patient

### Gestion de la Facturation
**Créer une facture :**
1. Menu "Facturation" → "Nouvelle facture"
2. Sélectionnez le patient
3. Ajoutez les prestations et montants
4. Indiquez la description des soins
5. Générez la facture

**Marquer une facture comme payée :**
1. Liste des factures → Sélectionnez
2. Cliquez "Marquer payée"
3. Indiquez la date de paiement
4. Sauvegardez

**Générer un PDF :**
1. Ouvrez la facture
2. Cliquez "Générer PDF"
3. Imprimez ou envoyez par email

### Accueil et Standard
**Gérer les appels :**
- Consultez le planning en temps réel
- Informez sur les disponibilités
- Prenez les rendez-vous par téléphone
- Gérez les urgences selon les procédures

**Accueil des patients :**
- Vérifiez les rendez-vous du jour
- Mettez à jour les coordonnées si nécessaire
- Orientez vers les salles d'attente
- Gérez les retards et annulations

## 💬 Communication et Notifications

### Chat Interne
**Utilisation du chat :**
1. Cliquez sur l'icône chat (bulle en bas à droite)
2. Sélectionnez un collègue dans la liste
3. Tapez votre message et appuyez sur Entrée
4. Les messages sont instantanés

**Statuts de connexion :**
- 🟢 En ligne : Disponible pour chat
- 🟡 Absent : Connecté mais inactif
- 🔴 Hors ligne : Non connecté

### Système de Notifications
**Types de notifications :**
- 📅 Nouveaux rendez-vous
- ⚠️ Annulations urgentes
- 💰 Factures en retard
- 📝 Nouvelles prescriptions
- 🔔 Messages importants

**Gestion des notifications :**
1. Cliquez sur l'icône cloche
2. Consultez les notifications non lues
3. Cliquez pour marquer comme lu
4. Archivez les anciennes notifications

## 🔧 Fonctionnalités Communes

### Recherche Globale
- Utilisez la barre de recherche en haut
- Recherchez patients, rendez-vous, factures
- Filtrez par date, statut, médecin
- Résultats instantanés

### Filtres et Tri
- Tous les tableaux sont triables
- Utilisez les filtres par date, statut, type
- Sauvegardez vos filtres favoris
- Exportez les données si nécessaire

### Raccourcis Clavier
- `Ctrl + N` : Nouveau (patient, rendez-vous, etc.)
- `Ctrl + S` : Sauvegarder
- `Ctrl + F` : Rechercher
- `Esc` : Fermer les modales

## 🆘 Résolution de Problèmes

### Problèmes de Connexion
**Mot de passe oublié :**
1. Page de connexion → "Mot de passe oublié"
2. Saisissez votre email
3. Consultez votre boîte mail
4. Suivez le lien de réinitialisation

**Session expirée :**
- Reconnectez-vous avec vos identifiants
- Vos données non sauvegardées peuvent être perdues
- Sauvegardez régulièrement votre travail

### Problèmes Techniques
**Page qui ne se charge pas :**
1. Actualisez la page (F5)
2. Vérifiez votre connexion internet
3. Videz le cache du navigateur
4. Contactez l'administrateur si le problème persiste

**Données manquantes :**
1. Vérifiez vos filtres de recherche
2. Élargissez la période de recherche
3. Contactez l'administrateur pour vérification

### Support Technique
**Contacter le support :**
- Email : support@belvicare.com
- Téléphone : +237 XXX XXX XXX
- Chat interne : Contactez l'administrateur
- Heures de support : 8h-18h, Lun-Ven

## 📋 Bonnes Pratiques

### Sécurité
- Ne partagez jamais vos identifiants
- Déconnectez-vous en fin de session
- Utilisez un mot de passe fort
- Signalez toute activité suspecte

### Confidentialité
- Respectez le secret médical
- Ne consultez que les dossiers nécessaires
- Fermez les dossiers après consultation
- Attention aux écrans en zone publique

### Efficacité
- Sauvegardez régulièrement votre travail
- Utilisez les raccourcis clavier
- Organisez votre planning à l'avance
- Communiquez les changements rapidement

### Qualité des Données
- Vérifiez l'exactitude des informations
- Mettez à jour les coordonnées patients
- Documentez les consultations
- Archivez les anciens dossiers

## 📞 Contacts Utiles

- **Support Technique** : support@belvicare.com
- **Formation** : formation@belvicare.com
- **Administration** : admin@belvicare.com
- **Urgences Système** : +237 XXX XXX XXX

---

*Ce guide est mis à jour régulièrement. Version actuelle : v1.0 - Janvier 2025*





# Clinical Management Backend

🏥 **Système de Gestion Clinique** - Backend API REST pour la gestion hospitalière complète

## 📋 Vue d'Ensemble

Application Spring Boot pour la gestion d'une clinique avec :
- 👥 Gestion des patients et utilisateurs
- 📅 Planification des rendez-vous
- 💊 Prescriptions médicales
- 💰 Facturation et paiements
- 💬 Chat temps réel et notifications
- 🔐 Sécurité JWT avec rôles hiérarchiques

## 🚀 Démarrage Rapide

### Prérequis
- Java 17+
- Maven 3.6+
- PostgreSQL
- Docker (optionnel pour MinIO)

### Installation

1. **Configuration de l'environnement**
```bash
cp env.template .env
# Éditez le fichier .env avec vos paramètres
```

2. **Lancement du stockage MinIO**
```bash
docker compose up minio --build -d
# ou si déjà construit :
docker compose up minio -d
```

3. **Compilation et démarrage**
```bash
mvn clean install
mvn spring-boot:run
```

4. **Accès à l'application**
- API : http://localhost:8888
- Documentation Swagger : http://localhost:8888/swagger-ui.html
- MinIO Console : http://localhost:9001

## 📚 Documentation

- 📖 **[Documentation Technique et Fonctionnelle](DOCUMENTATION_TECHNIQUE_FONCTIONNELLE.md)** - Architecture, modèles de données, API
- 👤 **[Guide Utilisateur](GUIDE_UTILISATEUR.md)** - Manuel d'utilisation par rôle

## 🏗️ Architecture

```
├── 🔐 Security (JWT, Roles)
├── 👥 User Management (Admin, Doctor, Secretary)
├── 🏥 Core Modules
│   ├── 📋 Patient Management
│   ├── 📅 Appointment Scheduling
│   ├── 💊 Prescription Management
│   └── 💰 Invoice Management
├── 💬 Communication
│   ├── 🔔 Notifications
│   └── 💬 Real-time Chat
└── 📊 Dashboards & Reports
```

## 🎯 Rôles et Permissions

| Rôle | Patients | Rendez-vous | Prescriptions | Factures | Admin |
|------|----------|-------------|---------------|----------|---------|
| **ADMIN** | ✅ Complet | ✅ Complet | ✅ Lecture | ✅ Complet | ✅ Complet |
| **DOCTOR** | ✅ Complet | ✅ Consultation/Annulation | ✅ Complet | ❌ | ❌ |
| **SECRETARY** | ✅ Complet | ✅ Gestion complète | ❌ | ✅ Complet | ❌ |

## 🔑 Comptes par Défaut

```
Admin     : admin@belvicare.com / password
Médecin   : doctor@belvicare.com / password
Secrétaire: secretary@belvicare.com / password
```

## 🛠️ Technologies

- **Backend** : Spring Boot 3.3.0, Java 17
- **Sécurité** : Spring Security, JWT
- **Base de données** : PostgreSQL, JPA/Hibernate
- **Documentation** : Swagger/OpenAPI 3.0
- **Stockage** : MinIO S3-compatible
- **Communication** : WebSocket
- **PDF** : iText, Apache PDFBox
- **Build** : Maven

## 📡 API Endpoints

### Authentification
- `POST /api/v1/auth/register` - Inscription
- `POST /api/v1/auth/login` - Connexion

### Gestion Core
- `GET|POST|PUT|DELETE /api/v1/patients` - Patients
- `GET|POST|PUT|DELETE /api/v1/appointments` - Rendez-vous
- `GET|POST|PUT|DELETE /api/v1/prescriptions` - Prescriptions
- `GET|POST|PUT|DELETE /api/v1/invoices` - Factures

### Communication
- `GET|POST /api/v1/notifications` - Notifications
- `GET|POST /api/v1/chat` - Messages
- `WebSocket /ws` - Chat temps réel

### Administration
- `GET|POST|PUT|DELETE /api/v1/admin/users` - Utilisateurs
- `GET /api/v1/dashboard/{role}` - Tableaux de bord

## 🧪 Tests

```bash
# Tests unitaires
mvn test

# Tests d'intégration
mvn integration-test

# Couverture de code
mvn jacoco:report
```

## 🐳 Docker

```bash
# Build de l'image
docker build -t clinical-management .

# Lancement complet
docker compose up -d
```

## 🔧 Configuration

Configurez les variables d'environnement dans `.env` :

```env
# Base de données
DATABASE_URL=jdbc:postgresql://localhost:5432/clinic_db
DATABASE_USERNAME=clinic_user
DATABASE_PASSWORD=clinic_password

# Application
BACKEND_PORT=8888
JWT_SECRET_KEY=your-secret-key

# MinIO
MINIO_API_URL=http://localhost:9000
MINIO_USERNAME=minio_user
MINIO_PASSWORD=minio_password
```

## 📈 Statut du Projet

- ✅ **v0.1.0** (2025) - Fonctionnalités core prêtes pour UAT
- 🧪 **En test** - Intégrations avancées
- ⏳ **Roadmap** - Modules télémédecine, mobile

## 🤝 Contribution

1. Fork le projet
2. Créez une branche feature (`git checkout -b feature/AmazingFeature`)
3. Committez vos changements (`git commit -m 'Add AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

## 📄 Licence

Ce projet est sous licence Apache 2.0 - voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 📞 Support

- 📧 Email : support@belvicare.com
- 📱 Téléphone : +237 XXX XXX XXX
- 💬 Chat : Via l'application

---aJOUE

*Développé avec ❤️ pour améliorer la gestion hospitalière*