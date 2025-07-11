import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

/**
 * The FlappyBird class represents the main game panel for the Flappy Bird game.
 * It handles game logic, rendering, and user input.
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

    /**
     * The Bird class represents the player's character.
     * It holds the bird's position, dimensions, and image.
     */
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
    int pipeWidth = 64;  // Scaled by 1/6
    int pipeHeight = 512;

    /**
     * The Pipe class represents the obstacles in the game.
     * It holds the pipe's position, dimensions, image, and a flag to track if the bird has passed it.
     */
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
    int velocityX = -4; // Speed at which pipes move to the left, simulating the bird moving right.
    int velocityY = 0;  // The bird's upward/downward speed.
    int gravity = 1;    // The force pulling the bird down.

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;

    /**
     * Constructor for the FlappyBird game panel.
     * Initializes the game window, loads images, sets up game objects, and starts the game timers.
     */
    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true); // Allows the panel to receive key events.
        addKeyListener(this);

        // Load images from resource files.
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Initialize the bird and the list of pipes.
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // Timer to place new pipes at a regular interval.
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipeTimer.start();

        // Main game loop timer, running at approximately 60 frames per second.
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    /**
     * Generates and places a new pair of top and bottom pipes off-screen to the right.
     * The vertical position of the pipes is randomized.
     */
    void placePipes() {
        // Calculate a random Y position for the top pipe.
        // The calculation ensures the pipe opening is within a reasonable vertical range.
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        // Create and add the top pipe to the list.
        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        // Create and add the bottom pipe, positioned relative to the top pipe.
        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    /**
     * Overrides the default paintComponent method to draw the game.
     * This is the entry point for all custom drawing in the JPanel.
     *
     * @param g the Graphics object to protect
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /**
     * Draws all game elements onto the screen.
     *
     * @param g The Graphics context used for drawing.
     */
    public void draw(Graphics g) {
        // Draw the background image, covering the entire panel.
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

        // Draw the bird.
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        // Draw all the pipes in the list.
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Draw the score.
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    /**
     * Updates the state of the game for each frame.
     * This includes moving the bird and pipes, and checking for collisions.
     */
    public void move() {
        // Update bird's vertical position based on gravity.
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0); // Prevent the bird from moving above the top of the screen.

        // Move all pipes to the left and check for collisions or scoring.
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            // Check if the bird has successfully passed a pipe to update the score.
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5; // Score is incremented by 0.5 for each pipe (top and bottom), so 1 point per pair.
                pipe.passed = true;
            }

            // Check for collision with the current pipe.
            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        // Check if the bird has fallen off the bottom of the screen.
        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    /**
     * Checks for collision between the bird and a pipe using Axis-Aligned Bounding Box (AABB) collision detection.
     *
     * @param a The Bird object.
     * @param b The Pipe object.
     * @return true if the bird and pipe are colliding, false otherwise.
     */
    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&   // a's top left corner doesn't reach b's top right corner
                a.x + a.width > b.x &&   // a's top right corner passes b's top left corner
                a.y < b.y + b.height &&  // a's top left corner doesn't reach b's bottom left corner
                a.y + a.height > b.y;    // a's bottom left corner passes b's top left corner
    }

    /**
     * This method is called by the gameLoop timer at each frame.
     * It triggers the game state update and repaints the screen.
     *
     * @param e The ActionEvent object.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint(); // Redraws the component by calling paintComponent.
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }

    /**
     * Handles key press events.
     * The space bar makes the bird "jump" and restarts the game if it's over.
     *
     * @param e The KeyEvent object.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            // Set the bird's vertical velocity to make it jump.
            velocityY = -9;

            // If the game is over, pressing space will restart it.
            if (gameOver) {
                // Reset game conditions to their initial state.
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                gameLoop.start();
                placePipeTimer.start();
            }
        }
    }

    /**
     * This method is part of the KeyListener interface but is not used in this game.
     *
     * @param e The KeyEvent object.
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * This method is part of the KeyListener interface but is not used in this game.
     *
     * @param e The KeyEvent object.
     */
    @Override
    public void keyReleased(KeyEvent e) {
    }
}