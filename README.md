# clinical-management-backend
This repository is dedicated to the clinical management backend project.

```
cp env.template .env
```

configure local minio service in docker

```
docker compose up minio --build -d

// or if already build just launch it
 
docker compose up minio -d
```

configure .env variable to be recognise by your ide (intellij, vscode)
