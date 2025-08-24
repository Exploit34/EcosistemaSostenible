import java.util.Random;

public class EcosistemaSostenible {

    public static void main(String[] args) {
        argsProcess(args);
    }

    public static void argsProcess(String[] args) {
        String mapa = ""; // variable donde recibe el mapa
        int width = 0, height = 0, generation = 0, speed = 0, direccion = 1; //variables de principales
        String widthError = "", heightError = "", generationError = "", speedError = "", direccionError = "", mapaError = ""; //variables de error si falla una de las variables

        // Se recibe los datos enviados de main
        for (String arg : args) {
            try {
                if (arg.startsWith("w=")) width = parseWidth(arg.substring(2));
                else if (arg.startsWith("h=")) height = parseHeight(arg.substring(2));
                else if (arg.startsWith("g=")) generation = parseGeneration(arg.substring(2));
                else if (arg.startsWith("s=")) speed = parseSpeed(arg.substring(2));
                else if (arg.startsWith("m=")) mapa = arg.substring(2);
                else if (arg.startsWith("n=")) direccion = parseDireccion(arg.substring(2));
            } catch (IllegalArgumentException e) {
                if (arg.startsWith("w=")) widthError = e.getMessage();
                else if (arg.startsWith("h=")) heightError = e.getMessage();
                else if (arg.startsWith("g=")) generationError = e.getMessage();
                else if (arg.startsWith("s=")) speedError = e.getMessage();
                else if (arg.startsWith("n=")) direccionError = e.getMessage();
                else if (arg.startsWith("m=")) mapaError = e.getMessage();
            }
        }

        printParameters(width, height, generation, speed, mapa, direccion, widthError, heightError, generationError, speedError, direccionError, mapaError); // imprime los parametros recibidos

        // Validadaciones obligatorias
        if (width == 0 || height == 0 || mapa.isEmpty()) {
            System.out.println("Error: Faltan parámetros obligatorios (w, h, m).");
            return;
        }

        // Crea el mapa inicial
        int[][] grid;
        if (mapa.equals("rnd")) { // si solo quiere que el valor sea aleatorio
            grid = generarMapaAleatorio(width, height); // genera aleatorio el mapa
        } else {
            try {
                grid = parseMapa(mapa, width, height); // parsea los valores de mapa
            } catch (IllegalArgumentException e) {
                System.out.println("Error en formato del mapa: " + e.getMessage()); // si falla el parseo de mapa
                return;
            }
        }

        // generaciones
        System.out.println("\nPoblacion inicial:");
        for (int gen = 1; (generation == 0 || gen <= generation); gen++) { // llega hasta el rango de las generaciones dadas
            System.out.println("\nGeneración " + gen);
            dibujarMapa(grid); // dibuja el mapa

            int[][] nuevo = aplicarReglas(grid, gen, direccion); // matriz

            if (gen % 2 == 0) { // generaciones en movimiento
                nuevo = moverAnimales(nuevo, direccion); // da direccion a al ecosistema
            }

            grid = nuevo; // guarda la matriz en la variable grid

            try {
                Thread.sleep(speed); // el hilo se pausa cuando llega al valor de speed
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // si el hilo se interrumpe o falla
            }
        }
    }

    // parseo de los parámetros
    public static int parseWidth(String valor) {
        int w = Integer.parseInt(valor);
        int[] valPermitidos = {5, 10, 15, 20, 40, 80};
        for (int p : valPermitidos) if (p == w) return w; // retorna el valor si es correcto
        throw new IllegalArgumentException("Ancho inválido 5, 10, 15, 20, 40, 80");
    }

    public static int parseHeight(String valor) {
        int h = Integer.parseInt(valor);
        int[] valPermitidos = {5, 10, 15, 20, 40};
        for (int p : valPermitidos) if (p == h) return h; // retorna el valor si es correcto
        throw new IllegalArgumentException("Alto inválido 5, 10, 15, 20, 40");
    }

    public static int parseGeneration(String valor) {
        int g = Integer.parseInt(valor);
        if (g < 0 || g > 1000) {
            throw new IllegalArgumentException("Generaciones solo de 0 a 1000");
        }
        return g;
    }

    public static int parseSpeed(String valor) {
        int s = Integer.parseInt(valor);
        int[] valPermitidos = {0, 250, 500, 1000, 5000};
        for (int p : valPermitidos) if (p == s) return s; // retorna el valor si es correcto
        throw new IllegalArgumentException("Velocidad inválida 0, 250, 500, 1000, 5000");
    }

    public static int parseDireccion(String valor) {
        int d = Integer.parseInt(valor);
        int[] valPermitidos = {1, 2, 3, 4};
        for (int p : valPermitidos) if (p == d) return d;
        throw new IllegalArgumentException("Dirección inválida 1, 2, 3, 4 (n es opcional)");
    }

    // mapa inicial
    public static int[][] generarMapaAleatorio(int width, int height) { // matriz de generar ecosistema aleatorio
        Random r = new Random(); // la libreria random
        int[][] grid = new int[height][width]; // matriz
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                grid[i][j] = r.nextInt(4); // 0..3
            }
        }
        return grid;
    }

    // parseo de los valores en el mapa
    public static int[][] parseMapa(String mapa, int width, int height) {
        String[] filas = mapa.split("#"); // se separa por #
        int[][] grid = new int[height][width];
        for (int i = 0; i < filas.length && i < height; i++) {
            String fila = filas[i];
            for (int j = 0; j < fila.length() && j < width; j++) {
                char c = fila.charAt(j);
                if (c < '0' || c > '3') {
                    throw new IllegalArgumentException("Carácter inválido: " + c);
                }
                grid[i][j] = c - '0';
            }
        }
        return grid;
    }

    // Reglas del juego
    public static int[][] aplicarReglas(int[][] grid, int gen, int dir) {
        int h = grid.length, w = grid[0].length;
        int[][] nuevo = new int[h][w];

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int cell = grid[i][j];

                if (cell == 0) { // celda vacía
                    // Árbol
                    if (contarDireccion(grid, i, j, 1, true) >= 2) {
                        nuevo[i][j] = 1;

                        // Animal (solo en generaciones impares)
                    } else if (gen % 2 == 1 &&
                            contarDireccion(grid, i, j, 2, true) == 2 &&
                            (contarDireccion(grid, i, j, 1, false) >= 1) &&
                            (contarDireccion(grid, i, j, 3, false) >= 1)) {
                        nuevo[i][j] = 2;

                        // Agua (si hay agua arriba y gen múltiplo de 3)
                    } else if (gen % 3 == 0 && hayAguaFilaArriba(grid, i, j)) {
                        nuevo[i][j] = 3;

                    } else {
                        nuevo[i][j] = 0;
                    }

                } else if (cell == 1) { // árbol
                    if (contarDireccion(grid, i, j, 3, false) >= 1) {
                        nuevo[i][j] = 1;
                    } else {
                        nuevo[i][j] = 0;
                    }

                } else if (cell == 2) { // animal
                    if (contarDireccion(grid, i, j, 1, false) >= 1 &&
                            contarDireccion(grid, i, j, 3, false) >= 1) {
                        nuevo[i][j] = 2;
                    } else {
                        nuevo[i][j] = 0;
                    }

                } else if (cell == 3) { // agua
                    nuevo[i][j] = 3;
                }
            }
        }
        return nuevo;
    }

    // movimiento de los animales
    public static int[][] moverAnimales(int[][] grid, int dir) {
        int h = grid.length, w = grid[0].length;
        int[][] nuevo = new int[h][w];

        // copiar el estado inicial
        for (int i = 0; i < h; i++) {
            nuevo[i] = grid[i].clone();
        }

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (grid[i][j] == 2) { // animal
                    int ni = i, nj = j;
                    if (dir == 1) nj++;      // derecha
                    else if (dir == 2) ni++; // abajo
                    else if (dir == 3) nj--; // izquierda
                    else if (dir == 4) ni--; // arriba

                    if (ni >= 0 && ni < h && nj >= 0 && nj < w) {
                        if (grid[ni][nj] == 0) { // destino vacío
                            nuevo[i][j] = 0;
                            nuevo[ni][nj] = 2;
                        } else if (grid[ni][nj] == 1) { // destino árbol → lo consume
                            nuevo[i][j] = 0;
                            nuevo[ni][nj] = 2;
                        } else if (grid[ni][nj] == 3) {
                            // destino agua → no se mueve (queda en el mismo sitio)
                            nuevo[i][j] = 2;
                        }
                    }
                }
            }
        }
        return nuevo;
    }

    // direciones o vecinos
    public static int contarDireccion(int[][] grid, int x, int y, int tipo, boolean soloInmediato) {
        int count = 0;
        int[][] offsets = soloInmediato ? offsetsInmediatos() : offsetsTodos();
        for (int[] d : offsets) {
            int nx = x + d[0], ny = y + d[1];
            if (nx >= 0 && nx < grid.length && ny >= 0 && ny < grid[0].length) {
                if (grid[nx][ny] == tipo) count++;
            }
        }
        return count; // 
    }

    public static boolean hayAguaFilaArriba(int[][] grid, int x, int y) {
        if (x == 0) return false;
        for (int dy = -1; dy <= 1; dy++) {
            int ny = y + dy;
            if (ny >= 0 && ny < grid[0].length) {
                if (grid[x - 1][ny] == 3) return true;
            }
        }
        return false;
    }

    public static int[][] offsetsInmediatos() { // desplazamiento relativo
        return new int[][]{
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1}, {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };
    }

    public static int[][] offsetsTodos() { // desplazamiento relativo
        // inmediato mas externo posiciones
        return new int[][]{
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1}, {0, 1},
                {1, -1}, {1, 0}, {1, 1},
                {-2, -2}, {-2, -1}, {-2, 0}, {-2, 1}, {-2, 2},
                {-1, -2}, {-1, 2},
                {0, -2}, {0, 2},
                {1, -2}, {1, 2},
                {2, -2}, {2, -1}, {2, 0}, {2, 1}, {2, 2}
        };
    }

    // imprime parametros recibidos y ya con las validaciones solo imprime si esta invalido y lo que debe oner el usuario si coloco mal el valor
    private static void printParameters(int width, int height, int generation, int speed, String map, int direccion,
                                        String widthError, String heightError, String generationError,
                                        String speedError, String direccionError, String mapaError) {
        System.out.println("\nParámetros recibidos:");
        System.out.printf("Width = [%s]%s%n", width != 0 ? width : "Inválido", widthError.isEmpty() ? "" : " -> " + widthError);
        System.out.printf("Height = [%s]%s%n", height != 0 ? height : "Inválido", heightError.isEmpty() ? "" : " -> " + heightError);
        System.out.printf("Generations = [%s]%s%n", generation != 0 ? generation : "No presente", generationError.isEmpty() ? "" : " -> " + generationError);
        System.out.printf("Speed = [%s]%s%n", speed != 0 ? speed + " ms" : "No presente", speedError.isEmpty() ? "" : " -> " + speedError);
        System.out.printf("Map = [%s]%s%n", !map.isEmpty() ? map : "No presente", mapaError.isEmpty() ? "" : " -> " + mapaError);
        System.out.printf("Direccion = [%s]%s%n", direccion >= 0 ? direccion : "No presente", direccionError.isEmpty() ? "" : " -> " + direccionError);
    }

    // dibuja el mapa que se ve por consola
    public static void dibujarMapa(int[][] grid) {
        for (int[] fila : grid) {
            for (int cell : fila) {
                switch (cell) {
                    case 0 -> System.out.print(" . "); // vacio
                    case 1 -> System.out.print("🌲 "); // arbol
                    case 2 -> System.out.print("🐑 "); // oveja
                    case 3 -> System.out.print("💧 "); // agua
                }
            }
            System.out.println(); // separa las generaciones
        }
    }
}
