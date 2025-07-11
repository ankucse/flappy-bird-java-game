import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

/**
 * The FlappyBird class represents the main game panel for the Flappy Bird game.
 * It handles game logic, rendering, and user input for multiple players and rounds.
 */
public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    // --- Game Board Dimensions ---
    int boardWidth = 360;
    int boardHeight = 640;

    // --- Image Assets ---
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // --- Bird Properties ---
    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // --- Pipe Properties ---
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // --- Game Logic Variables ---
    Bird bird;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;

    // --- New Game State & Score Variables ---
    boolean gameOver = false;
    boolean gameStarted = false; // To show initial start message
    double currentTurnScore = 0;

    // --- New Multiplayer and Rounds Variables ---
    int numPlayers;
    int numRounds;
    int currentPlayer;
    int currentRound;
    double[] totalScores; // Stores total score for each player across all rounds
    boolean allRoundsComplete = false;

    /**
     * Constructor for the FlappyBird game panel.
     * Initializes the game based on the number of players and rounds.
     *
     * @param numPlayers The number of players in the game.
     * @param numRounds  The total number of rounds to be played.
     */
    FlappyBird(int numPlayers, int numRounds) {
        this.numPlayers = numPlayers;
        this.numRounds = numRounds;
        this.totalScores = new double[numPlayers];
        this.currentPlayer = 1;
        this.currentRound = 1;

        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Initialize game objects
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // Timers are started by the first key press
        placePipeTimer = new Timer(1500, e -> placePipes());
        gameLoop = new Timer(1000 / 60, this);
    }

    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Draw background and bird
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        // Draw pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Draw Score and Game State Text
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));

        if (allRoundsComplete) {
            drawFinalScores(g);
        } else if (gameOver) {
            // Message between turns
            g.drawString("Player " + currentPlayer + " Score: " + (int) currentTurnScore, 10, 35);
            g.setFont(new Font("Arial", Font.BOLD, 28));

            int nextPlayer = (currentPlayer % numPlayers) + 1;
            int nextRound = currentRound + (currentPlayer / numPlayers);

            g.drawString("Press SPACE for Player " + nextPlayer + "'s turn.", 20, boardHeight / 2);
            g.drawString("(Round " + nextRound + "/" + numRounds + ")", 100, boardHeight / 2 + 40);
        } else if (!gameStarted) {
            // Initial start message
            g.drawString("Player 1", 130, boardHeight / 2 - 40);
            g.drawString("Round 1/" + numRounds, 110, boardHeight / 2);
            g.drawString("Press SPACE to Start", 40, boardHeight / 2 + 40);
        } else {
            // In-game HUD
            String scoreText = "Score: " + (int) currentTurnScore;
            String playerText = "P" + currentPlayer + " | R" + currentRound + "/" + numRounds;
            g.drawString(scoreText, 10, 35);
            g.drawString(playerText, boardWidth - g.getFontMetrics().stringWidth(playerText) - 10, 35);
        }
    }

    /**
     * Draws the final scoreboard after all rounds are completed.
     *
     * @param g The Graphics context.
     */
    private void drawFinalScores(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Final Results", 70, 100);

        g.setFont(new Font("Arial", Font.PLAIN, 28));
        double winnerScore = -1;
        ArrayList<Integer> winners = new ArrayList<>();

        // Find the highest score among all players
        for (double score : totalScores) {
            if (score > winnerScore) {
                winnerScore = score;
            }
        }

        // Find all players who achieved the highest score (to handle ties)
        if (winnerScore >= 0) { // Check >= 0 in case the winning score is 0
            for (int i = 0; i < totalScores.length; i++) {
                if (totalScores[i] == winnerScore) {
                    winners.add(i + 1); // Player numbers are 1-based
                }
            }
        }

        // Display the final scores for each player
        int yPos = 160;
        for (int i = 0; i < numPlayers; i++) {
            String scoreLine = "Player " + (i + 1) + ": " + (int) totalScores[i];
            g.drawString(scoreLine, 100, yPos);
            yPos += 40;
        }

        // Announce the winner(s)
        g.setFont(new Font("Arial", Font.BOLD, 32));
        yPos += 20; // Add some vertical space
        if (winners.size() == 1) {
            g.drawString("Winner: Player " + winners.get(0) + "!", 80, yPos);
        } else if (winners.size() > 1) {
            // Build and center the text for a tie
            StringBuilder winnerText = new StringBuilder("Winners: ");
            for (int i = 0; i < winners.size(); i++) {
                winnerText.append("P").append(winners.get(i));
                if (i < winners.size() - 1) {
                    winnerText.append(", ");
                }
            }
            winnerText.append("!");
            int textWidth = g.getFontMetrics().stringWidth(winnerText.toString());
            g.drawString(winnerText.toString(), (boardWidth - textWidth) / 2, yPos);
        } else {
            g.drawString("No winner!", 110, yPos);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Press 'R' to Restart", 80, boardHeight - 100);
    }

    public void move() {
        // Bird physics
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0); // Prevent bird from flying off the top of the screen

        // Move pipes and check for scoring
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            // Score increases when the bird successfully passes a pair of pipes
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                currentTurnScore += 0.5; // 0.5 for each pipe (top/bottom) = 1 point per pair
            }

            // Check for collision with a pipe
            if (collision(bird, pipe)) {
                handleGameOver();
                return; // Stop further checks once a collision is detected
            }
        }

        // Check for collision with the bottom of the screen
        if (bird.y > boardHeight) {
            handleGameOver();
        }
    }

    /**
     * Handles the game over logic for the current player's turn.
     */
    private void handleGameOver() {
        gameOver = true;
        totalScores[currentPlayer - 1] += currentTurnScore; // Add turn score to player's total
        gameLoop.stop();
        placePipeTimer.stop();
    }

    public boolean collision(Bird a, Pipe b) {
        // Simple rectangle collision detection
        return a.x < b.x + b.width &&   // a's top left corner doesn't reach b's top right
                a.x + a.width > b.x &&   // a's top right corner passes b's top left
                a.y < b.y + b.height &&  // a's top left corner doesn't reach b's bottom left
                a.y + a.height > b.y;    // a's bottom left corner passes b's top left
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // This is the main game loop, called by the gameLoop Timer
        move();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (allRoundsComplete) {
                return; // Game is fully over, do nothing on space press
            }

            if (gameOver) {
                // --- A player's turn ended, transition to the next turn ---
                currentPlayer++;
                if (currentPlayer > numPlayers) {
                    currentPlayer = 1;
                    currentRound++;
                }

                if (currentRound > numRounds) {
                    allRoundsComplete = true;
                    repaint(); // Show final scores and stop the game
                } else {
                    // Reset and start the game for the next player
                    bird.y = birdY;
                    velocityY = -9; // Give an initial flap to start the turn
                    pipes.clear();
                    currentTurnScore = 0;
                    gameOver = false;
                    gameStarted = true;
                    gameLoop.start();
                    placePipeTimer.start();
                }
            } else {
                // --- Player is actively playing, so flap the bird ---
                velocityY = -9;

                // If it's the very first press of the entire game, start the timers
                if (!gameStarted) {
                    gameStarted = true;
                    gameLoop.start();
                    placePipeTimer.start();
                }
            }
        } else if (e.getKeyCode() == KeyEvent.VK_R) {
            // Allow restarting only from the final score screen
            if (allRoundsComplete) {
                restartGame();
            }
        }
    }

    /**
     * Resets the entire game to its initial state, ready for a new match.
     * This is called when 'R' is pressed on the final score screen.
     */
    private void restartGame() {
        // Reset all game state variables
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();

        totalScores = new double[numPlayers];
        currentPlayer = 1;
        currentRound = 1;
        currentTurnScore = 0;

        gameOver = false;
        gameStarted = false;
        allRoundsComplete = false;

        // Timers are already stopped. They will be started on the first key press.
        repaint();
    }

    // --- Unused KeyListener methods ---
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}