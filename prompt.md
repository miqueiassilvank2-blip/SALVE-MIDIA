Crie um app Android NATIVO (APK instalável) chamado Salve Midia, replicando o design da imagem 638.png desta pasta. Sem servidor, tudo roda dentro do app.

STACK
- Kotlin + Jetpack Compose + Material3, minSdk 24, targetSdk 34
- Biblioteca io.github.junkfood02.youtubedl-android (library + ffmpeg)
- Coil para miniaturas, DataStore para configurações, Room para histórico
- Pacote: com.salvemidia.app

DESIGN
- Tema escuro fixo: fundo #0D0D14, roxo #6D28F9 (degradê até #7C3AED), cards #16161F com cantos 16dp, texto branco e secundário #8A8A99

TELAS
1. Início: cabeçalho com ícone roxo de download, título Salve (branco) + Mídia (roxo), subtítulo "Baixe vídeos e áudios de suas redes favoritas" com exclamação no fim, e engrenagem para Configurações. Abas Buscar e Por Link. Buscar: campo + botão Pesquisar (ytsearch10 via yt-dlp, lista com miniatura, título, duração e botão Baixar). Por Link: campo + botão Baixar por Link. Seção Redes Suportadas com grade 4x2 (YouTube, TikTok, Instagram, Facebook, X, Kwai, Vimeo, E muito mais) com aviso ao tocar.
2. Histórico: lista (título, rede, data) com botão limpar.
3. Downloads: arquivos baixados com Abrir, Compartilhar e Excluir.
4. Configurações: qualidade padrão, formato padrão (MP4 ou MP3), limpar histórico.
- Barra de navegação inferior: Início, Histórico, Downloads.

FUNCIONAMENTO
- Diálogo ao baixar: Vídeo (360p, 720p, 1080p) ou Áudio (MP3)
- Download em Foreground Service com notificação e barra de progresso
- Salvar em Download/SalveMidia via MediaStore
- Pedir permissão de notificações
- Toasts de erro claros
- Ícone de launcher roxo com seta branca

ENTREGA
- Projeto Gradle completo e funcional (settings.gradle.kts, app/build.gradle.kts, manifest, código, recursos)
- .github/workflows/build.yml que compila o APK debug (JDK 17, gradle assembleDebug) e publica como artifact
- README com os passos para subir no GitHub e baixar o APK
