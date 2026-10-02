import subprocess

def falar(texto):
    subprocess.run(["termux-tts-speak", texto])

while True:
    falar("Estou ouvindo.")
    
    resultado = subprocess.run(
        ["termux-speech-to-text"],
        capture_output=True,
        text=True
    )
    
    comando = resultado.stdout.strip()

    if not comando:
        continue

    print("Você:", comando)

    if comando.lower() in ["sair", "parar", "encerrar"]:
        falar("Até mais, Anderson.")
        break

    resposta = f"Você disse: {comando}"
    print("IA:", resposta)
    falar(resposta)
