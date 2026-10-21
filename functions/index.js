const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { onCall, onRequest, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const admin = require("firebase-admin");

admin.initializeApp();

async function enviarFCMParaUsuario(uid, dados) {
  const snapshot = await admin.firestore()
    .collection("usuarios")
    .document(uid)
    .collection("dispositivos")
    .get();

  const tokens = snapshot.docs
    .map((doc) => String(doc.get("token") || doc.id || "").trim())
    .filter(Boolean);

  if (tokens.length === 0) return { enviados: 0, falhos: 0 };

  let enviados = 0;
  let falhos = 0;

  for (let i = 0; i < tokens.length; i += 500) {
    const lote = tokens.slice(i, i + 500);
    const resposta = await admin.messaging().sendEachForMulticast({
          tokens: lote,
          notification: {
            title: String(dados.title || dados.titulo || "Nova Mensagem"),
            body: String(dados.body || dados.texto || dados.mensagem || "")
          },
          data: dados,
          android: {
            priority: "high",
            ttl: 0,
            notification: {
              channelId: "danike_high_priority_v2",
              priority: "max",
              visibility: "public",
              defaultSound: true,
              defaultVibrateTimings: true
            }
          }
        });

    enviados += resposta.successCount;
    falhos += resposta.failureCount;
  }

  return { enviados, falhos };
}


const CLOUDINARY_CLOUD_NAME = defineSecret("CLOUDINARY_CLOUD_NAME");
const CLOUDINARY_API_KEY = defineSecret("CLOUDINARY_API_KEY");
const CLOUDINARY_API_SECRET = defineSecret("CLOUDINARY_API_SECRET");

exports.excluirFotoEquipe = onCall(
  {
    secrets: [
      CLOUDINARY_CLOUD_NAME,
      CLOUDINARY_API_KEY,
      CLOUDINARY_API_SECRET
    ]
  },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError(
        "unauthenticated",
        "Usuário não autenticado."
      );
    }

    const email =
      request.auth.token.email || "";

    if (email.toLowerCase() !== "lipesanderson@gmail.com") {
      throw new HttpsError(
        "permission-denied",
        "Somente o administrador pode excluir fotos da equipe."
      );
    }

    const publicId =
      String(request.data?.publicId || "").trim();

    const idEquipe =
      String(request.data?.idEquipe || "").trim();

    if (!publicId || !idEquipe) {
      throw new HttpsError(
        "invalid-argument",
        "publicId e idEquipe são obrigatórios."
      );
    }

    const timestamp =
      Math.floor(Date.now() / 1000);

    const crypto =
      require("crypto");

    const signatureBase =
      `public_id=${publicId}&timestamp=${timestamp}` +
      CLOUDINARY_API_SECRET.value();

    const signature =
      crypto
        .createHash("sha1")
        .update(signatureBase)
        .digest("hex");

    const body =
      new URLSearchParams({
        public_id: publicId,
        timestamp: String(timestamp),
        api_key: CLOUDINARY_API_KEY.value(),
        signature
      });

    const resposta =
      await fetch(
        `https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME.value()}/image/destroy`,
        {
          method: "POST",
          headers: {
            "Content-Type":
              "application/x-www-form-urlencoded"
          },
          body
        }
      );

    const resultado =
      await resposta.json();

    if (!resposta.ok ||
        resultado.result !== "ok" &&
        resultado.result !== "not found") {
      console.error(
        "Erro Cloudinary:",
        resultado
      );

      throw new HttpsError(
        "internal",
        "Não foi possível excluir a foto do Cloudinary."
      );
    }

    await admin
      .firestore()
      .collection("equipe")
      .doc(idEquipe)
      .delete();

    return {
      sucesso: true,
      cloudinary: resultado.result
    };
  }
);


// Upload da foto pessoal do usuário autenticado.
// Mantém separado o fluxo de fotos exclusivas da equipe.
exports.uploadFotoPerfil = onRequest(
  {
    secrets: [
      CLOUDINARY_CLOUD_NAME,
      CLOUDINARY_API_KEY,
      CLOUDINARY_API_SECRET
    ],
    maxInstances: 5
  },
  async (req, res) => {
    res.set("Cache-Control", "no-store");

    if (req.method !== "POST") {
      res.set("Allow", "POST");
      return res.status(405).json({ erro: "Use POST para enviar a foto." });
    }

    try {
      const authorization = String(req.headers.authorization || "");
      const match = authorization.match(/^Bearer\s+(.+)$/i);

      if (!match) {
        return res.status(401).json({ erro: "Usuário não autenticado." });
      }

      await admin.auth().verifyIdToken(match[1]);

      const tipo = String(req.headers["content-type"] || "")
        .split(";")[0].trim().toLowerCase();

      const extensoes = {
        "image/jpeg": "jpg",
        "image/png": "png",
        "image/webp": "webp"
      };

      if (!extensoes[tipo]) {
        return res.status(415).json({
          erro: "Envie uma imagem JPEG, PNG ou WebP."
        });
      }

      const imagem = req.rawBody;
      if (!imagem || !imagem.length || imagem.length > 8 * 1024 * 1024) {
        return res.status(400).json({
          erro: "A foto está vazia ou ultrapassa 8 MB."
        });
      }

      const cloud = CLOUDINARY_CLOUD_NAME.value();
      const apiKey = CLOUDINARY_API_KEY.value();
      const secret = CLOUDINARY_API_SECRET.value();
      const timestamp = Math.floor(Date.now() / 1000);
      const folder = "danikeai/perfis";

      const crypto = require("crypto");
      const assinaturaBase =
        `folder=${folder}&timestamp=${timestamp}${secret}`;
      const signature = crypto
        .createHash("sha1")
        .update(assinaturaBase)
        .digest("hex");

      const formulario = new FormData();
      formulario.append(
        "file",
        new Blob([imagem], { type: tipo }),
        `foto.${extensoes[tipo]}`
      );
      formulario.append("api_key", apiKey);
      formulario.append("timestamp", String(timestamp));
      formulario.append("folder", folder);
      formulario.append("signature", signature);

      const resposta = await fetch(
        `https://api.cloudinary.com/v1_1/${cloud}/image/upload`,
        { method: "POST", body: formulario }
      );

      const resultado = await resposta.json();

      if (!resposta.ok || !resultado.secure_url ||
          !resultado.secure_url.startsWith("https://")) {
        console.error("Falha no upload da foto pessoal:", {
          status: resposta.status,
          error: resultado.error?.message || "Resposta inválida"
        });
        return res.status(502).json({
          erro: "Não foi possível enviar a foto ao serviço de imagens."
        });
      }

      return res.status(200).json({
        ok: true,
        secure_url: resultado.secure_url,
        public_id: resultado.public_id
      });
    } catch (e) {
      console.error("Erro em uploadFotoPerfil:", e.message);
      const status = e.code === "auth/id-token-expired" ||
                     e.code === "auth/argument-error" ||
                     e.code === "auth/invalid-id-token"
        ? 401 : 500;
      return res.status(status).json({
        erro: status === 401
          ? "Sessão inválida. Entre novamente no aplicativo."
          : "Não foi possível enviar a foto. Tente novamente."
      });
    }
  }
);

const TIPOS_NOTIFICACAO = new Set([
  "mensagem",
  "aviso",
  "atualizacao",
  "manutencao",
  "geral"
]);

exports.enviarNotificacao = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError(
      "unauthenticated",
      "É necessário estar autenticado."
    );
  }

  const email = String(
    request.auth.token.email || ""
  ).trim().toLowerCase();

  if (email !== "lipesanderson@gmail.com") {
    throw new HttpsError(
      "permission-denied",
      "Apenas o administrador pode enviar notificações."
    );
  }

  const dados = request.data || {};

  const tipo = String(dados.tipo || "")
    .trim()
    .toLowerCase();

  const titulo = String(dados.titulo || "").trim();
  const texto = String(dados.texto || "").trim();

  const uid = String(dados.uid || "").trim();
  const token = String(dados.token || "").trim();
  const topico = String(dados.topico || "").trim();

  if (!TIPOS_NOTIFICACAO.has(tipo)) {
    throw new HttpsError(
      "invalid-argument",
      "Tipo de notificação inválido."
    );
  }

  if (!titulo || !texto) {
    throw new HttpsError(
      "invalid-argument",
      "Título e texto são obrigatórios."
    );
  }

  const destinos = [uid, token, topico]
    .filter(Boolean)
    .length;

  if (destinos !== 1) {
    throw new HttpsError(
      "invalid-argument",
      "Informe exatamente um destino: uid, token ou topico."
    );
  }

  const mensagem = {
    data: {
      tipo,
      titulo,
      texto
    },
    android: {
      priority: "high"
    }
  };

  if (token) {
    const resposta = await admin.messaging().send({
      ...mensagem,
      token
    });

    return {
      sucesso: true,
      enviados: 1,
      falhos: 0,
      messageId: resposta
    };
  }

  if (topico) {
    const resposta = await admin.messaging().send({
      ...mensagem,
      topic: topico
    });

    return {
      sucesso: true,
      enviados: 1,
      falhos: 0,
      messageId: resposta
    };
  }

  const snapshot = await admin.firestore()
    .collection("usuarios")
    .document(uid)
    .collection("dispositivos")
    .get();

  const tokens = snapshot.docs
    .map((doc) => String(
      doc.get("token") || doc.id || ""
    ).trim())
    .filter(Boolean);

  if (tokens.length === 0) {
    return {
      sucesso: false,
      enviados: 0,
      falhos: 0,
      dispositivos: 0,
      motivo: "Nenhum dispositivo registrado para este usuário."
    };
  }

  let enviados = 0;
  let falhos = 0;

  for (let i = 0; i < tokens.length; i += 500) {
    const lote = tokens.slice(i, i + 500);

    const resposta = await admin.messaging()
      .sendEachForMulticast({
        tokens: lote,
        data: mensagem.data,
        android: mensagem.android
      });

    enviados += resposta.successCount;
    falhos += resposta.failureCount;

    const tokensInvalidos = [];

    resposta.responses.forEach((resultado, indice) => {
      const codigo = resultado.error?.code || "";

      if (
        codigo === "messaging/registration-token-not-registered" ||
        codigo === "messaging/invalid-registration-token"
      ) {
        tokensInvalidos.push(lote[indice]);
      }
    });

    await Promise.all(
      tokensInvalidos.map((tokenInvalido) =>
        admin.firestore()
          .collection("usuarios")
          .document(uid)
          .collection("dispositivos")
          .document(tokenInvalido)
          .delete()
      )
    );
  }

  return {
    sucesso: enviados > 0,
    enviados,
    falhos,
    dispositivos: tokens.length
  };
});

exports.notificarNovoConvite = onDocumentCreated(
  "convites/{uidDestino}/recebidos/{uidRemetente}",
  async (event) => {
    const convite = event.data?.data();
    if (!convite || convite.status !== "pendente") {
      return null;
    }

    const uidDestino = event.params.uidDestino;
    const uidRemetente = event.params.uidRemetente;

    const perfil = await admin.firestore()
      .collection("usuarios")
      .document(uidRemetente)
      .get();

    const nome = String(perfil.get("nome") || "Alguém").trim();

    await enviarFCMParaUsuario(uidDestino, {
      tipo: "convite",
      titulo: "Nova solicitação de conexão",
      texto: `${nome} quer se conectar com você.`,
      remetenteUid: uidRemetente
    });

    return null;
  }
);

exports.notificarConviteAceito = onDocumentUpdated(
  "convites/{uidDestino}/recebidos/{uidRemetente}",
  async (event) => {
    const antes = event.data?.before?.data();
    const depois = event.data?.after?.data();

    if (!antes || !depois) {
      return null;
    }

    if (antes.status === depois.status || depois.status !== "aceito") {
      return null;
    }

    const uidDestino = event.params.uidDestino;
    const uidRemetente = event.params.uidRemetente;

    const perfil = await admin.firestore()
      .collection("usuarios")
      .document(uidDestino)
      .get();

    const nome = String(perfil.get("nome") || "Alguém").trim();

    await enviarFCMParaUsuario(uidRemetente, {
      tipo: "conexao_aceita",
      titulo: "Conexão aceita!",
      texto: `${nome} aceitou sua solicitação de conexão.`,
      remetenteUid: uidDestino
    });

    return null;
  }
);

exports.notificarNovaMensagem = onDocumentCreated(
  "chats/{conversaId}/mensagens/{mensagemId}",
  async (event) => {
    const mensagem = event.data?.data();
    if (!mensagem) {
      return null;
    }

    const destinatarioUid = String(mensagem.destinatarioUid || "").trim();
    const remetenteUid = String(mensagem.remetenteUid || "").trim();
    const texto = String(mensagem.texto || "").trim();
    const tipoMensagem = String(mensagem.tipo || "").trim();

    if (
      !destinatarioUid ||
      !remetenteUid ||
      !texto ||
      destinatarioUid === remetenteUid ||
      tipoMensagem !== "texto"
    ) {
      return null;
    }

    const perfil = await admin.firestore()
      .collection("usuarios")
      .document(remetenteUid)
      .get();

    const nome = String(perfil.get("nome") || "Nova mensagem").trim();

    await enviarFCMParaUsuario(destinatarioUid, {
      tipo: "mensagem",
      titulo: nome,
      texto,
      remetenteUid
    });

    return null;
  }
);
