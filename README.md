# Spring Boot GCP Integration

A Spring Boot 3.x based User Management Application deployed to Google Cloud using Cloud Run, Cloud SQL, Secret Manager, and CI/CD via Cloud Build.

## Features

- Spring Boot 3.x application
- GCP Cloud Run deployment (dev and prod environments)
- Cloud SQL PostgreSQL integration
- Artifact Registry for container images
- Secret management with GCP Secret Manager
- CI/CD with Cloud Build Triggers (GitHub integration)
- IAM and Service Account-based security and access control

## Prerequisites

- Google Cloud Project with billing enabled
- Enabled APIs:
  - Cloud Run API
  - Cloud SQL Admin API
  - Secret Manager API
  - Artifact Registry API
  - Cloud Build API
- Java 17+ JDK
- Maven 3.8+
- GitHub repo connected to Google Cloud Build


## 🔐 IAM & Service Account Setup

### Required IAM Roles

| Role                             | Description                                 |
|----------------------------------|---------------------------------------------|
| `Cloud Run Admin`                | Deploy to Cloud Run                         |
| `Cloud Build Service Account`   | Allow Cloud Build to access other services  |
| `Cloud SQL Client`              | Connect to Cloud SQL from Cloud Run         |
| `Secret Manager Secret Accessor`| Access secrets at runtime                   |
| `Artifact Registry Reader`      | Pull Docker image from Artifact Registry    |

### Service Account Setup

#### 1. Create a dedicated deployer service account:

```bash
gcloud iam service-accounts create cloud-build-deployer   --display-name="Cloud Build Deployer"
```

#### 2. Grant required roles to the deployer:

```bash
PROJECT_ID=$(gcloud config get-value project)

gcloud projects add-iam-policy-binding $PROJECT_ID   --member="serviceAccount:cloud-build-deployer@$PROJECT_ID.iam.gserviceaccount.com"   --role="roles/run.admin"

gcloud projects add-iam-policy-binding $PROJECT_ID   --member="serviceAccount:cloud-build-deployer@$PROJECT_ID.iam.gserviceaccount.com"   --role="roles/cloudsql.client"

gcloud projects add-iam-policy-binding $PROJECT_ID   --member="serviceAccount:cloud-build-deployer@$PROJECT_ID.iam.gserviceaccount.com"   --role="roles/secretmanager.secretAccessor"

gcloud projects add-iam-policy-binding $PROJECT_ID   --member="serviceAccount:cloud-build-deployer@$PROJECT_ID.iam.gserviceaccount.com"   --role="roles/artifactregistry.reader"
```

### Grant Cloud Build permission to impersonate the deployer

```bash
gcloud iam service-accounts add-iam-policy-binding cloud-build-deployer@$PROJECT_ID.iam.gserviceaccount.com   --member="serviceAccount:$PROJECT_NUMBER@cloudbuild.gserviceaccount.com"   --role="roles/iam.serviceAccountUser"
```

## 🔑 Secret Manager Setup

#### Create secrets for each environment:

```bash
# For dev
echo -n 'your-dev-password' | gcloud secrets create db-dev-password   --replication-policy="automatic" --data-file=-

# For prod
echo -n 'your-prod-password' | gcloud secrets create db-prod-password   --replication-policy="automatic" --data-file=-
```

## 🛢️ Cloud SQL Setup

```bash
# Create a PostgreSQL instance
gcloud sql instances create userdb-dev   --database-version=POSTGRES_14   --tier=db-f1-micro   --region=us-central1

# Create a user for the database
gcloud sql users create springuser --instance=userdb-dev --password=<PASSWORD>

# Create a database
gcloud sql databases create userdb --instance=userdb-dev
```

## 🚀 Cloud Build Triggers Setup

### Create Trigger for Dev Environment

```bash
gcloud builds triggers create cloud-source-repositories   --name="dev-trigger"   --repo="user-mgmt-app"   --branch-pattern="^dev$"   --build-config="cloudbuild.yaml"   --substitutions=_ENV="dev",_INSTANCE_NAME="userdb-dev",_DB_SECRET="db-dev-password"
```

### Create Trigger for Prod Environment

```bash
gcloud builds triggers create cloud-source-repositories   --name="prod-trigger"   --repo="user-mgmt-app"   --branch-pattern="^main$"   --build-config="cloudbuild.yaml"   --substitutions=_ENV="prod",_INSTANCE_NAME="userdb-prod",_DB_SECRET="db-prod-password"
```

## How to Deploy

1. Commit code to the `develop` branch (for dev) or `main` branch (for prod).
2. Cloud Build trigger fires and runs the steps defined in `cloudbuild.yaml`.
3. App is built, containerized, and deployed to Cloud Run.
4. Secrets are fetched, database is connected securely, and the app starts serving.

---

## Architecture Overview

```
GitHub → Cloud Build Trigger → Docker Build → Artifact Registry → Cloud Run Deploy
                                                ↓
                                        Secret Manager / Cloud SQL
```


## CI/CD Workflow

This project uses Google Cloud Build with GitHub integration to automate deployments.

1. **GitHub Integration**:
   - Source code is hosted on GitHub.
   - Two build triggers are created in Google Cloud Build for **dev** and **prod** environments.
   - Each trigger listens to branches (e.g., `master` for prod, `dev` for dev).

2. **Build & Deploy**:
   - On each push to the corresponding branch, Cloud Build performs the following:
     - **Build Docker Image** with Spring profile (`dev` or `prod`).
     - **Push Image** to Artifact Registry.
     - **Deploy to Cloud Run** with environment-specific configuration.
     - Secrets are securely pulled from **Secret Manager**.
     - Cloud SQL instance is automatically connected using **Cloud SQL Auth Proxy**.

📸 **Snapshots / Screenshots**: _[Include screenshots of Cloud Run service, SQL instance, triggers, and logs in README for better understanding if hosting on GitHub]_

![image](https://github.com/user-attachments/assets/410df4b9-1290-4dc2-a8d4-952db621aa76)

![image](https://github.com/user-attachments/assets/52929d0e-3a47-440e-a4e7-9ca6e40e0584)

![image](https://github.com/user-attachments/assets/98329b67-ce43-4284-9f57-1fc639cb7fa5)

![image](https://github.com/user-attachments/assets/99842a9e-72c3-487d-83e3-ac872147c621)

![image](https://github.com/user-attachments/assets/d779c1d2-ab2f-4ea0-8f16-8c3cbbe93595)

![image](https://github.com/user-attachments/assets/3db5c40f-cc0a-4978-9aec-850207ee0ed5)

![image](https://github.com/user-attachments/assets/2d488849-a7bb-4634-ba4b-edb2d84054bd)

![image](https://github.com/user-attachments/assets/8991e420-a8cf-44e0-b2a1-17ca855b0a4e)

![image](https://github.com/user-attachments/assets/2245ce20-90ac-42f5-8ee6-22bc15f6ef9f)

Enjoy deploying Spring Boot apps on Google Cloud with full CI/CD support! 🚀

