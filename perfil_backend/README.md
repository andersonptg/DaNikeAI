# DaNikeAI-perfil

Serviço independente para upload e exclusão de fotos de perfil.

Rotas:
- GET /health
- POST /profile/upload-photo
- POST /profile/delete-photo

O upload recebe a imagem binária diretamente, com Content-Type image/jpeg,
image/png ou image/webp, e exige Firebase ID token no cabeçalho Bearer.

O serviço usa Cloudinary e restringe os identificadores ao diretório
perfis/<UID>/ do usuário autenticado.

Variáveis no Render:
- FIREBASE_PROJECT_ID=da-nike-films
- CLOUDINARY_CLOUD_NAME
- CLOUDINARY_API_KEY
- CLOUDINARY_API_SECRET

Não configure segredos no código-fonte ou no GitHub.
