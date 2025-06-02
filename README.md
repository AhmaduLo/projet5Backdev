# Yoga App !

# 📌 Présentation

Ce projet constitue la partie back-end de l’application Yoga Studio, 
développée en Java 11 et utilisant MySQL comme base de données. 
Il permet la gestion des sessions de yoga et l’authentification des utilisateurs (admin et standard).


# ⚙️ Technologies utilisées

Java 11

Spring Boot

MySQL

JPA / Hibernate

JUnit 5

Mockito

Spring Security

Jacoco (pour la couverture de test)

# 🚀 Lancer le projet 

Cloner le projet : 
git clone https://github.com/AhmaduLo/projet5Backdev.git

Lancer le back-end : ./mvnw spring-boot:run

# 1. Prérequis

Java 11

Maven

MySQL installé sur le port 3306 (par défaut)

IDE compatible (IntelliJ, Eclipse...)

# 2. Installation de la base de données

Crée une base de données nommée user ;

Assure-toi que les identifiants DB dans application.properties ou application.yml correspondent :

spring.datasource.url=jdbc:mysql://localhost:3306/test

spring.datasource.username=user

spring.datasource.password=123456

# 3. Démarrer l’API

Depuis la racine du projet : mvn spring-boot:run

# 🧪 Lancer les tests:

Exécuter tous les tests : mvn test

# Comptes de test

Compte admin

Email : yoga@studio.com

Mot de passe : test!1234