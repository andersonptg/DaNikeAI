import json
import threading
import unittest
from http.server import ThreadingHTTPServer
from urllib.error import HTTPError
from urllib.request import Request, urlopen
from unittest.mock import patch

import server


class TestPerfilBackend(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.httpd = ThreadingHTTPServer(
            ("127.0.0.1", 0), server.PerfilHandler
        )
        cls.thread = threading.Thread(
            target=cls.httpd.serve_forever, daemon=True
        )
        cls.thread.start()
        cls.base = "http://127.0.0.1:" + str(cls.httpd.server_port)

    @classmethod
    def tearDownClass(cls):
        cls.httpd.shutdown()
        cls.httpd.server_close()

    def request(self, path, body=b"", headers=None):
        req = Request(
            self.base + path,
            data=body if body else None,
            headers=headers or {},
            method="POST",
        )
        try:
            with urlopen(req, timeout=5) as response:
                return response.status, response.read()
        except HTTPError as erro:
            return erro.code, erro.read()

    def test_health(self):
        with urlopen(self.base + "/health", timeout=5) as resposta:
            self.assertEqual(resposta.status, 200)
            self.assertTrue(json.loads(resposta.read())["ok"])

    def test_upload_sem_token_retorna_401(self):
        status, _ = self.request(
            "/profile/upload-photo",
            b"imagem",
            {"Content-Type": "image/jpeg"},
        )
        self.assertEqual(status, 401)

    def test_delete_sem_token_retorna_401(self):
        status, _ = self.request(
            "/profile/delete-photo",
            b'{"publicId":"perfis/uid123/foto"}',
            {"Content-Type": "application/json"},
        )
        self.assertEqual(status, 401)

    def test_uid_invalido(self):
        self.assertFalse(server.validar_uid("../outro"))
        self.assertFalse(server.validar_uid(""))

    def test_foto_de_outro_usuario_bloqueada(self):
        self.assertFalse(
            server.validar_public_id("perfis/outro/foto123", "meu_uid")
        )

    def test_public_id_com_traversal_bloqueado(self):
        self.assertFalse(
            server.validar_public_id("perfis/meu_uid/../outra", "meu_uid")
        )

    def test_public_id_proprio_aceito(self):
        self.assertTrue(
            server.validar_public_id("perfis/meu_uid/foto123", "meu_uid")
        )

    def test_delete_foto_alheia_retorna_403(self):
        with patch.object(
            server, "verificar_token_firebase",
            return_value={"sub": "meu_uid"},
        ):
            status, _ = self.request(
                "/profile/delete-photo",
                b'{"publicId":"perfis/outro/foto123"}',
                {
                    "Authorization": "Bearer token-teste",
                    "Content-Type": "application/json",
                },
            )
        self.assertEqual(status, 403)


if __name__ == "__main__":
    unittest.main(verbosity=2)
