import com.formdev.flatlaf.FlatLightLaf;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    private static Font montserratBold;

    
    // Assets
    
    private static final String imagem = "src/Assets/Images/LogoCompleto.png";
    private static final String imagemAPP = "src/Assets/Images/Logo.png";

    
    // GUI base
    
    private static CardLayout layout;
    private static JFrame frame;
    private static JPanel panel;

    
    // Screens
    
    private static JPanel menu;
    private static JPanel settings;
    private static JPanel quiz;

    
    // Quiz logic + UI
    
    private static final QuestionBank bank = new QuestionBank();
    private static QuizSession session;

    private static JLabel quizPergunta;
    private static JLabel quizProgresso;
    private static JLabel quizScore;
    private static JButton[] botoesResposta = new JButton[4];

    
    // Theme
    
    private static final Color COR_FUNDO = new Color(70, 23, 143);
    private static final Color[] CORES_KAHOOT = new Color[]{
            new Color(224, 37, 60),   // Vermelho - 0
            new Color(19, 104, 206),  // Azul - 1
            new Color(216, 158, 0),   // Amarelo - 2
            new Color(38, 137, 12)    // Verde - 3
    };

    public static void main(String[] args) {
        
        // Setup
        
        configurarFlatLaf();
        carregarFont();
        imageAppDock();

        // Música (mantive exatamente a chamada como tinhas)
        Music.load("src/Assets/Music/Music1.wav");
        Music.playLoop();

        
        // Start UI
        
        MenuPrincipal();
    }

    private static void MenuPrincipal() {
        
        // Criar componentes (menu)
        
        JLabel logoLabel = new JLabel(new ImageIcon(imagem));
        Image scaled = new ImageIcon(imagem).getImage().getScaledInstance(768, 512, Image.SCALE_SMOOTH);
        logoLabel.setIcon(new ImageIcon(scaled));

        JButton startButton = new JButton("Começar");
        CustomTextField inputField = new CustomTextField("Digite o seu nome", montserratBold);
        JButton settingsButton = new JButton("Definições");

        
        // Criar componentes (settings)
        
        JButton settingsVoltarButton = new JButton("Voltar");
        JButton musicPlayButton = new JButton("Tocar música");
        JButton musicStopButton = new JButton("Parar música");

        
        // Criar base dos ecrãs / container
        
        layout = new CardLayout();
        frame = new JFrame("BrainQuiz");

        panel = new JPanel(layout);
        menu = new JPanel();
        settings = new RoundedPanel(30);
        quiz = new JPanel();

        ImagemAppWindows();

        
        // Frame (propriedades)
        
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.getContentPane().setBackground(COR_FUNDO);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridBagLayout());

        
        // Panel container (propriedades)
        
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setBackground(COR_FUNDO);

        
        // Menu (propriedades)
        
        menu.setBackground(COR_FUNDO);

        
        // Settings (propriedades + layout + alinhamentos)
        
        settings.setBackground(new Color(0, 0, 0, 180));

        settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS));
        musicPlayButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        musicStopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingsVoltarButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        
        // Settings (adds)
        
        settings.add(Box.createVerticalStrut(20));
        settings.add(musicPlayButton);
        settings.add(Box.createVerticalStrut(10));
        settings.add(musicStopButton);
        settings.add(Box.createVerticalStrut(20));
        settings.add(settingsVoltarButton);

        
        // Criar QUIZ UI
        
        buildQuizScreen();

        
        // Ações (listeners)
        
        musicPlayButton.addActionListener(e -> Music.playLoop());
        musicStopButton.addActionListener(e -> Music.stop());

        startButton.addActionListener(e -> {
            String nome = inputField.getText();

            if (nome == null || nome.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Por favor, digite o seu nome!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            session = new QuizSession(nome.trim(), bank.getRandomQuestions(10));
            mostrarPergunta();
            layout.show(panel, "quiz");
        });

        settingsButton.addActionListener(e -> layout.show(panel, "settings"));
        settingsVoltarButton.addActionListener(e -> layout.show(panel, "menu"));

        
        // Layout menu (propriedades + alinhamentos)
        
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputField.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        inputField.setMaximumSize(inputField.getPreferredSize());

        
        // Menu (adds)
        
        menu.add(Box.createVerticalStrut(10));
        menu.add(logoLabel);
        menu.add(Box.createVerticalStrut(10));
        menu.add(inputField);
        menu.add(Box.createVerticalStrut(15));
        menu.add(startButton);
        menu.add(Box.createVerticalStrut(15));
        menu.add(settingsButton);

        
        // Cards (adds no panel)
        
        panel.add(menu, "menu");
        panel.add(settings, "settings");
        panel.add(quiz, "quiz");

        
        // Frame (add final + show)
        
        frame.add(panel, new GridBagConstraints());
        layout.show(panel, "menu");
        frame.setVisible(true);
    }

    
    // QUIZ UI (cores Kahoot)
    
    private static void buildQuizScreen() {
        // Propriedades base
        quiz.setBackground(COR_FUNDO);
        quiz.setLayout(new BorderLayout(15, 15));
        quiz.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        
        // Topo (progresso + score)
        
        JPanel topoQuiz = new JPanel(new BorderLayout());
        topoQuiz.setOpaque(false);

        quizProgresso = new JLabel("0/0");
        quizProgresso.setForeground(Color.WHITE);
        quizProgresso.setFont(new Font("Arial", Font.BOLD, 16));

        quizScore = new JLabel("Score: 0");
        quizScore.setForeground(Color.WHITE);
        quizScore.setFont(new Font("Arial", Font.BOLD, 16));

        // Topo (adds)
        topoQuiz.add(quizProgresso, BorderLayout.WEST);
        topoQuiz.add(quizScore, BorderLayout.EAST);

        
        // Pergunta
        
        quizPergunta = new JLabel("Pergunta aqui");
        quizPergunta.setForeground(Color.WHITE);
        quizPergunta.setHorizontalAlignment(SwingConstants.CENTER);
        quizPergunta.setFont(new Font("Arial", Font.BOLD, 26));

        
        // Respostas 2x2
        
        JPanel respostas = new JPanel(new GridLayout(2, 2, 15, 15));
        respostas.setOpaque(false);

        for (int i = 0; i < 4; i++) {
            JButton b = new JButton("Opção " + (i + 1));
            final int index = i;

            // Estilo Kahoot
            b.setBackground(CORES_KAHOOT[i]);
            b.setForeground(Color.WHITE);
            b.setFont(new Font("Arial", Font.BOLD, 20));
            b.setOpaque(true);
            b.setContentAreaFilled(true);
            b.setBorderPainted(false);
            b.setFocusPainted(false);

            // Listener
            b.addActionListener(e -> responder(index));

            // Guardar + add
            botoesResposta[i] = b;
            respostas.add(b);
        }

        
        // Botão sair
        
        JButton sair = new JButton("Sair");
        sair.addActionListener(e -> layout.show(panel, "menu"));

        
        // Fundo baixo (respostas + sair)
        
        JPanel fundoBaixo = new JPanel(new BorderLayout(10, 10));
        fundoBaixo.setOpaque(false);

        fundoBaixo.add(respostas, BorderLayout.CENTER);
        fundoBaixo.add(sair, BorderLayout.SOUTH);

        
        // Quiz (adds finais)
        
        quiz.add(topoQuiz, BorderLayout.NORTH);
        quiz.add(quizPergunta, BorderLayout.CENTER);
        quiz.add(fundoBaixo, BorderLayout.SOUTH);
    }

    private static void mostrarPergunta() {
        if (session == null) return;

        Question q = session.getCurrentQuestion();

        // HTML para permitir quebra de linha e ficar centrado
        quizPergunta.setText("<html><div style='text-align:center; width:900px;'>" + q.getText() + "</div></html>");

        String[] opcoes = q.getOptions();
        for (int i = 0; i < 4; i++) {
            botoesResposta[i].setText(opcoes[i]);
            botoesResposta[i].setEnabled(true);
        }

        quizProgresso.setText((session.getCurrentIndex() + 1) + "/" + session.getTotalQuestions());
        quizScore.setText("Score: " + session.getScore());
    }

    private static void responder(int index) {
        if (session == null) return;

        session.answer(index);

        if (session.isFinished()) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Fim do Quiz!\nJogador: " + session.getPlayerName() + "\nPontuação: " + session.getScore(),
                    "Resultado",
                    JOptionPane.INFORMATION_MESSAGE
            );
            layout.show(panel, "menu");
            return;
        }

        mostrarPergunta();
    }

    
    // Configurações
    
    private static void configurarFlatLaf() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Button.arc", 20);
            UIManager.put("Component.arc", 20);
            UIManager.put("TextComponent.arc", 20);
        } catch (Exception ex) {
            System.err.println("Erro FlatLaf: " + ex.getMessage());
        }
    }

    private static void carregarFont() {
        try {
            montserratBold = Font.createFont(Font.TRUETYPE_FONT, new File("src/Assets/Font/Montserrat-Bold.ttf"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(montserratBold);
        } catch (Exception e) {
            System.err.println("Erro ao carregar fonte: " + e.getMessage());
            montserratBold = new Font("Arial", Font.BOLD, 18);
        }
    }

    
    // Personalização da App
    
    private static void imageAppDock() {
        ImageIcon icone = new ImageIcon(imagemAPP);
        Image imageToSet = icone.getImage();
        try {
            if (Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();
                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    taskbar.setIconImage(imageToSet);
                }
            }
        } catch (UnsupportedOperationException | SecurityException e) {
            System.err.println("Erro ao definir ícone da dock: " + e.getMessage());
        }
    }

    private static void ImagemAppWindows() {
        ImageIcon icone = new ImageIcon(imagemAPP);
        frame.setIconImage(icone.getImage());
    }
}


// Música

class Music {

    private static Clip clip;

    public static void load(String path) {
        try {
            File file = new File(path);
            AudioInputStream audio = AudioSystem.getAudioInputStream(file);

            clip = AudioSystem.getClip();
            clip.open(audio);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playLoop() {
        if (clip == null) return;

        clip.setFramePosition(0); // começa do início
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();
    }

    public static void play() {
        if (clip == null) return;

        clip.setFramePosition(0);
        clip.start();
    }

    public static void stop() {
        if (clip == null) return;

        clip.stop();
    }

    public static void pause() {
        if (clip == null) return;

        clip.stop(); // mantém a posição atual
    }
}

// UI

class CustomTextField extends JTextField {
    public CustomTextField(String placeholder, Font font) {
        super();
        setPreferredSize(new Dimension(300, 40));
        setBackground(Color.WHITE);
        setForeground(Color.DARK_GRAY);
        putClientProperty("JTextField.placeholderText", placeholder);

        if (font != null) setFont(font.deriveFont(16f));
    }
}

class RoundedPanel extends JPanel {
    private final int radius;

    public RoundedPanel(int radius) {
        this.radius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }
}

// Lógica

class Question {
    private final String text;
    private final String[] options; // 4 opções
    private final int correctIndex; // 0..3
    private final int points;

    public Question(String text, String[] options, int correctIndex, int points) {
        if (options == null || options.length != 4)
            throw new IllegalArgumentException("options tem de ter 4 itens");
        if (correctIndex < 0 || correctIndex > 3)
            throw new IllegalArgumentException("correctIndex tem de ser 0..3");

        this.text = text;
        this.options = options;
        this.correctIndex = correctIndex;
        this.points = points;
    }

    public String getText() { return text; }
    public String[] getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }
    public int getPoints() { return points; }

    public boolean isCorrect(int chosenIndex) {
        return chosenIndex == correctIndex;
    }
}

class QuestionBank {
    private final ArrayList<Question> questions = new ArrayList<>();

    public QuestionBank() {
        perguntasDefault();
    }

    // COMO ADICIONAR UMA PERGUNTA
//    questions.add(new Question(
//                "Texro da pergunta",
//                        new String[]{
//        "Opção 0",
//        "Opção 1",
//        "Opção 2",
//        "Opção 3",
    //    },
//            Opção de 0-3, PONTUAÇÃO
//            ));

    private void perguntasDefault() {

        questions.add(new Question(
                "O que são comportamentos aditivos?",
                new String[]{
                        "Comportamentos relacionados apenas ao consumo de drogas ilegais",
                        "Comportamentos repetitivos que geram dependência e perda de controlo",
                        "Hábitos saudáveis praticados regularmente",
                        "Problemas exclusivamente genéticos"
                },
                1, 100
        ));

        questions.add(new Question(
                "Qual das seguintes pode ser considerada uma dependência comportamental?",
                new String[]{
                        "Leitura",
                        "Exercício físico moderado",
                        "Jogo patológico",
                        "Alimentação equilibrada"
                },
                2, 100
        ));

        questions.add(new Question(
                "Um fator de risco para o desenvolvimento de dependências é:",
                new String[]{
                        "Boa comunicação familiar",
                        "Elevada autoestima",
                        "Pressão dos pares",
                        "Prática regular de desporto"
                },
                2, 100
        ));

        questions.add(new Question(
                "A prevenção primária tem como objetivo:",
                new String[]{
                        "Tratar casos graves de dependência",
                        "Evitar que o problema surja",
                        "Internar pessoas dependentes",
                        "Aplicar medicação"
                },
                1, 100
        ));

        questions.add(new Question(
                "A prevenção secundária consiste em:",
                new String[]{
                        "Impedir o primeiro contacto com a substância",
                        "Identificar precocemente e intervir nos primeiros sinais",
                        "Punir comportamentos de risco",
                        "Ignorar sinais iniciais"
                },
                1, 100
        ));

        questions.add(new Question(
                "Um exemplo de prevenção terciária é:",
                new String[]{
                        "Campanhas escolares informativas",
                        "Terapia e reabilitação de pessoas dependentes",
                        "Atividades recreativas",
                        "Palestras motivacionais"
                },
                1, 100
        ));

        questions.add(new Question(
                "Qual destas substâncias pode causar dependência?",
                new String[]{
                        "Álcool",
                        "Água",
                        "Oxigénio",
                        "Vitaminas"
                },
                0, 100
        ));

        questions.add(new Question(
                "A dependência química caracteriza-se por:",
                new String[]{
                        "Consumo ocasional sem consequências",
                        "Necessidade física e psicológica da substância",
                        "Uso controlado e responsável",
                        "Escolha consciente sem impacto"
                },
                1, 100
        ));

        questions.add(new Question(
                "Um fator de proteção é:",
                new String[]{
                        "Isolamento social",
                        "Conflitos familiares constantes",
                        "Apoio familiar",
                        "Falta de supervisão"
                },
                2, 100
        ));

        questions.add(new Question(
                "A adolescência é um período de maior vulnerabilidade porque:",
                new String[]{
                        "O cérebro ainda está em desenvolvimento",
                        "Já existe maturidade total",
                        "Não existem influências externas",
                        "Não há curiosidade"
                },
                0, 100
        ));

        questions.add(new Question(
                "O consumo excessivo de álcool pode levar a:",
                new String[]{
                        "Melhoria da memória",
                        "Dependência e problemas de saúde",
                        "Aumento permanente da concentração",
                        "Melhor desempenho escolar"
                },
                1, 100
        ));

        questions.add(new Question(
                "As campanhas de sensibilização têm como objetivo:",
                new String[]{
                        "Incentivar o consumo moderado",
                        "Informar e prevenir comportamentos de risco",
                        "Promover substâncias legais",
                        "Punir consumidores"
                },
                1, 100
        ));

        questions.add(new Question(
                "A dependência do jogo é reconhecida pela:",
                new String[]{
                        "Organização Mundial da Saúde",
                        "Polícia Internacional",
                        "Ministério da Educação",
                        "Organização Mundial do Comércio"
                },
                0, 100
        ));

        questions.add(new Question(
                "Um sinal de alerta de dependência é:",
                new String[]{
                        "Capacidade de parar facilmente",
                        "Perda de controlo sobre o comportamento",
                        "Uso esporádico",
                        "Ausência de consequências"
                },
                1, 100
        ));

        questions.add(new Question(
                "A prevenção em meio escolar deve incluir:",
                new String[]{
                        "Apenas punições",
                        "Informação, diálogo e desenvolvimento de competências sociais",
                        "Suspensão imediata",
                        "Exclusão de alunos"
                },
                1, 100
        ));

        questions.add(new Question(
                "As redes sociais podem tornar-se problemáticas quando:",
                new String[]{
                        "São usadas com equilíbrio",
                        "Não interferem na rotina",
                        "Substituem atividades essenciais e causam isolamento",
                        "São utilizadas para estudar"
                },
                2, 100
        ));

        questions.add(new Question(
                "A intervenção precoce:",
                new String[]{
                        "Agrava a situação",
                        "Não tem impacto",
                        "Pode evitar o agravamento da dependência",
                        "Substitui totalmente a prevenção"
                },
                2, 100
        ));

        questions.add(new Question(
                "O apoio psicológico é importante porque:",
                new String[]{
                        "Resolve tudo imediatamente",
                        "Ajuda a compreender causas e desenvolver estratégias",
                        "Substitui medicamentos sempre",
                        "É desnecessário"
                },
                1, 100
        ));

        questions.add(new Question(
                "A família tem um papel importante na prevenção porque:",
                new String[]{
                        "Não influencia comportamentos",
                        "Pode estabelecer limites e oferecer apoio",
                        "Deve ignorar sinais",
                        "Deve evitar diálogo"
                },
                1, 100
        ));

        questions.add(new Question(
                "A promoção de estilos de vida saudáveis ajuda a:",
                new String[]{
                        "Aumentar comportamentos de risco",
                        "Reduzir a probabilidade de dependências",
                        "Incentivar consumo experimental",
                        "Substituir totalmente políticas públicas"
                },
                1, 100
        ));
    }

    public List<Question> getRandomQuestions(int amount) {
        ArrayList<Question> copy = new ArrayList<>(questions);
        Collections.shuffle(copy);
        return new ArrayList<>(copy.subList(0, Math.min(amount, copy.size())));
    }
}

class QuizSession {
    private final String playerName;
    private final List<Question> quizQuestions;
    private int currentIndex = 0;
    private int score = 0;

    public QuizSession(String playerName, List<Question> quizQuestions) {
        this.playerName = playerName;
        this.quizQuestions = quizQuestions;
    }

    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public int getCurrentIndex() { return currentIndex; }
    public int getTotalQuestions() { return quizQuestions.size(); }

    public Question getCurrentQuestion() {
        return quizQuestions.get(currentIndex);
    }

    public void answer(int chosenIndex) {
        Question q = getCurrentQuestion();
        if (q.isCorrect(chosenIndex)) score += q.getPoints();
        currentIndex++;
    }

    public boolean isFinished() {
        return currentIndex >= quizQuestions.size();
    }
}