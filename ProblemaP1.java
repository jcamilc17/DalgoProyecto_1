import java.util.Scanner;

public class ProblemaP1 {

    // ============================================================
    // Convierte el texto en líneas respetando un ancho máximo.
    // No divide palabras.
    // Si una palabra es más larga que el ancho, retorna null.
    // Complejidad: O(n)
    // ============================================================
    public static String[] textToLines(String texto, int anchoLinea) {
        // Separar el texto en palabras usando espacio como delimitador
        String[] palabras = texto.split(" ");
        // Lista dinámica para almacenar las líneas generadas
        java.util.ArrayList<String> lineas = new java.util.ArrayList<>();
        // Línea que se está construyendo actualmente
        String lineaActual = "";
        for (String palabra : palabras) {
            // Si una palabra es más larga que el ancho permitido,
            // no existe una partición válida para este ancho
            if (palabra.length() > anchoLinea) {
                return null;
            }
            // Si la línea está vacía, simplemente agregamos la palabra
            if (lineaActual.isEmpty()) {
                lineaActual = palabra;
            }
            // Si la palabra cabe (considerando un espacio extra entre palabras)
            else if (lineaActual.length() + 1 + palabra.length() <= anchoLinea) {
                lineaActual += " " + palabra;
            }
            // Si no cabe, cerramos la línea actual y comenzamos una nueva
            else {
                lineas.add(lineaActual);
                lineaActual = palabra;
            }
        }
        // Agregar la última línea si no quedó vacía
        if (!lineaActual.isEmpty()) {
            lineas.add(lineaActual);
        }
        // Convertir ArrayList a arreglo y retornarlo
        return lineas.toArray(new String[0]);
    }

    // ============================================================
    // Calcula el río tipográfico más largo usando Programación
    // Dinámica directamente sobre String[] (sin matriz 2D).
    //
    // dp[i][j] = longitud del río que empieza en línea i,
    // columna j.
    //
    // Se permite moverse hacia abajo:
    // misma columna, izquierda o derecha (±1).
    //
    // Complejidad: O(n)
    // ============================================================
    public static int findLongestRiverOptimized(String[] lines) {
        // Caso base: texto vacío o nulo
        if (lines == null || lines.length == 0) {
            return 0;
        }
        int numRows = lines.length;
        int maxRiver = 0;
        // Crear estructura DP donde cada fila puede tener diferente tamaño
        int[][] dp = new int[numRows][];
        for (int i = 0; i < numRows; i++) {
            dp[i] = new int[lines[i].length()];
        }
        // ============================
        // Caso base: última fila
        // ============================
        // En la última fila, un espacio válido aporta longitud 1
        for (int j = 0; j < lines[numRows - 1].length(); j++) {
            // Solo cuentan espacios que NO estén al final de línea
            if (lines[numRows - 1].charAt(j) == ' '
                    && j < lines[numRows - 1].length() - 1) {
                dp[numRows - 1][j] = 1;
                maxRiver = 1;
            } else {
                dp[numRows - 1][j] = 0;
            }
        }
        // ============================
        // Llenar DP de abajo hacia arriba
        // ============================
        for (int i = numRows - 2; i >= 0; i--) {
            for (int j = 0; j < lines[i].length(); j++) {
                // Si no es espacio o es espacio final,
                // no puede iniciar un río
                if (lines[i].charAt(j) != ' '
                        || j >= lines[i].length() - 1) {
                    dp[i][j] = 0;
                    continue;
                }
                int maxNext = 0;
                // Revisar las tres posiciones posibles en la fila inferior
                // (izquierda, misma columna, derecha)
                int[] offsets = {-1, 0, 1};
                for (int delta : offsets) {
                    // Nueva columna candidata en la fila inferior
                    // representa movimiento -1, 0 o +1
                    int newCol = j + delta;
                    // Verificar que la columna esté dentro de límites
                    if (newCol >= 0
                            && newCol < lines[i + 1].length()) {

                        maxNext = Math.max(maxNext,
                                dp[i + 1][newCol]);
                    }
                }
                // Longitud del río desde esta posición
                // = 1 (espacio actual) + mejor continuación abajo
                dp[i][j] = 1 + maxNext;

                // Actualizar máximo global encontrado
                maxRiver = Math.max(maxRiver, dp[i][j]);
            }
        }
        return maxRiver;
    }

    // ============================================================
    // Busca el ancho óptimo que maximiza el río más largo.
    //
    // Explora todos los anchos posibles desde:
    // - minWidth = palabra más larga
    // - maxWidth = min(longitud total - 1, 500)
    //
    // Aplica optimizaciones:
    // 1. Saltar configuraciones imposibles
    // 2. Saltar si no puede mejorar el mejor resultado
    // 3. Early stopping si se alcanza el máximo teórico
    // ============================================================
    public static int[] findOptimalWidthAndRiver(String text) {
        // Calcular ancho mínimo posible (la palabra más larga)
        String[] words = text.split(" ");
        int minWidth = 0;
        for (String word : words) {
            minWidth = Math.max(minWidth, word.length());
        }
        // Definir ancho máximo razonable
        // Se limita a 500 para evitar explorar anchos excesivamente grandes
        int totalLength = text.length();
        int maxWidth = Math.min(totalLength - 1, 5000);
        // Si el ancho máximo es menor que el mínimo posible,
        // no existe configuración válida
        if (maxWidth < minWidth) {
            return new int[]{minWidth, 0};
        }
        // Inicialmente asumimos que el mejor ancho es el mínimo
        int bestWidth = minWidth;
        // Longitud máxima de río encontrada hasta el momento
        int bestRiver = 0;
        // Probar todos los anchos posibles
        for (int width = minWidth; width <= maxWidth; width++) {
            String[] lines = textToLines(text, width);
            // Si la partición no es válida o hay menos de 2 líneas,
            // no puede existir río
            if (lines == null || lines.length < 2) {
                continue;
            }
            // Optimización importante:
            // Un río no puede ser más largo que el número total de líneas,
            // porque cada línea solo puede aportar 1 unidad de longitud.
            // Si el número de líneas es menor que el mejor río ya encontrado,
            // esta configuración jamás podrá superarlo.
            if (lines.length < bestRiver) {
                continue;
            }
            int river = findLongestRiverOptimized(lines);
            // Actualizar mejor solución encontrada
            if (river > bestRiver) {
                bestRiver = river;
                bestWidth = width;
            }
            // Early stopping:
            // Si el río ocupa todas las líneas disponibles,
            // se alcanzó el máximo teórico posible
            if (river == lines.length) {
                break;
            }
        }
        return new int[]{bestWidth, bestRiver};
    }

    public static void main(String[] args) {
        Scanner scanner = new java.util.Scanner(System.in);
        
        int t = Integer.parseInt(scanner.nextLine().trim());
        
        for (int i = 0; i < t; i++) {
            String texto = scanner.nextLine().trim();
            int[] result = findOptimalWidthAndRiver(texto);
            System.out.println(result[0] + " " + result[1]);
        }
        
        scanner.close();
    }

}
