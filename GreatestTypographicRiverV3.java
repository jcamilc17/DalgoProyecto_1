public class GreatestTypographicRiverV3 {

    public static String[] textToLines(String texto, int anchoLinea) {
        String[] palabras = texto.split(" ");
        java.util.ArrayList<String> lineas = new java.util.ArrayList<>();
        String lineaActual = "";        
        for (String palabra : palabras) {
            // Validación: palabra más larga que el ancho
            if (palabra.length() > anchoLinea) {
                return null;
            }            
            if (lineaActual.isEmpty()) {
                lineaActual = palabra;
            } else if (lineaActual.length() + 1 + palabra.length() <= anchoLinea) {
                lineaActual += " " + palabra;
            } else {
                lineas.add(lineaActual);
                lineaActual = palabra;
            }
        }   
        if (!lineaActual.isEmpty()) {
            lineas.add(lineaActual);
        }      
        return lineas.toArray(new String[0]);
    }

    /**
     * Versión optimizada que trabaja directamente con String[]
     */
    public static int findLongestRiverOptimized(String[] lines) {
        if (lines == null || lines.length == 0) {
            return 0;
        }        
        int numRows = lines.length;
        int maxRiver = 0;
        int[][] dp = new int[numRows][];        
        for (int i = 0; i < numRows; i++) {
            dp[i] = new int[lines[i].length()];
        }        
        // Última fila
        for (int j = 0; j < lines[numRows - 1].length(); j++) {
            if (lines[numRows - 1].charAt(j) == ' ' && 
                j < lines[numRows - 1].length() - 1) {
                dp[numRows - 1][j] = 1;
                maxRiver = Math.max(maxRiver, 1);
            } else {
                dp[numRows - 1][j] = 0;
            }
        }        
        // DP de abajo hacia arriba
        for (int i = numRows - 2; i >= 0; i--) {
            for (int j = 0; j < lines[i].length(); j++) {
                if (lines[i].charAt(j) != ' ' || j >= lines[i].length() - 1) {
                    dp[i][j] = 0;
                    continue;
                }                
                int maxNext = 0;
                int[] offsets = {-1, 0, 1};                
                for (int delta : offsets) {
                    int newCol = j + delta;                    
                    if (newCol >= 0 && newCol < lines[i + 1].length()) {
                        if (dp[i + 1][newCol] > 0) {
                            maxNext = Math.max(maxNext, dp[i + 1][newCol]);
                        }
                    }
                }                
                dp[i][j] = 1 + maxNext;
                maxRiver = Math.max(maxRiver, dp[i][j]);
            }
        }        
        return maxRiver;
    }

    /**
     * Compara si dos arrays de strings son iguales
     */
    private static boolean arraysEqual(String[] a, String[] b) {
        if (a == null || b == null) return false;
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (!a[i].equals(b[i])) return false;
        }
        return true;
    }

    /**
     * Encuentra el siguiente ancho donde el layout cambia significativamente
     * 
     * @param text Texto completo
     * @param currentWidth Ancho actual
     * @param currentLines Layout actual
     * @param maxWidth Ancho máximo a considerar
     * @return Siguiente ancho donde cambia el layout, o currentWidth + 1 si no hay cambio cercano
     */
    private static int findNextBreakpoint(String text, int currentWidth, 
                                         String[] currentLines, int maxWidth) {
        if (currentLines == null) {
            return currentWidth + 1;
        }        
        int currentNumLines = currentLines.length;
        
        // Buscar el siguiente ancho donde cambie el número de líneas
        // Limitamos la búsqueda a un rango razonable para no hacer demasiadas llamadas
        int searchLimit = Math.min(currentWidth + 50, maxWidth);        
        for (int w = currentWidth + 1; w <= searchLimit; w++) {
            String[] newLines = textToLines(text, w);            
            if (newLines == null) {
                continue;
            }            
            // Si cambia el número de líneas, este es un breakpoint importante
            if (newLines.length != currentNumLines) {
                return w;
            }            
            // Si el contenido de las líneas cambió, también es un breakpoint
            if (!arraysEqual(newLines, currentLines)) {
                return w;
            }
        }        
        // Si no encontramos cambio en el rango, avanzar al siguiente
        return currentWidth + 1;
    }
    /**
     * OPTIMIZACIÓN 3: Saltar a anchos que cambien el layout
     * Encuentra el ancho óptimo saltando breakpoints
     */
    public static int[] findOptimalWidthAndRiverWithJumps(String text) {
        String[] words = text.split(" ");
        int minWidth = 0;
        for (String word : words) {
            minWidth = Math.max(minWidth, word.length());
        }        
        int totalLength = text.length();
        int maxWidth = Math.min(totalLength - 1, 500);        
        if (maxWidth < minWidth) {
            return new int[]{minWidth, 0};
        }        
        int bestWidth = minWidth;
        int bestRiver = 0;
        String[] prevLines = null;
        int width = minWidth;
        int iterationsCount = 0; // Para medir cuántas iteraciones reales hacemos        
        while (width <= maxWidth) {
            String[] lines = textToLines(text, width);            
            if (lines == null || lines.length < 2) {
                width++;
                continue;
            }            
            // Solo calcular si el layout cambió respecto al anterior
            boolean shouldCalculate = (prevLines == null) || 
                                     (lines.length != prevLines.length) ||
                                     (!arraysEqual(lines, prevLines));            
            if (shouldCalculate) {
                iterationsCount++;                
                // Optimización: skip si no puede mejorar
                if (lines.length >= bestRiver) {
                    int river = findLongestRiverOptimized(lines);                    
                    if (river > bestRiver) {
                        bestRiver = river;
                        bestWidth = width;
                    }                    
                    // Early stopping
                    if (river == lines.length) {
                        break;
                    }
                }                
                prevLines = lines;
            }            
            // Saltar al siguiente breakpoint
            int nextWidth = findNextBreakpoint(text, width, lines, maxWidth);
            width = nextWidth;
        }        
        // Retornar también el número de iteraciones para comparar
        System.err.println("Iteraciones con saltos: " + iterationsCount);
        return new int[]{bestWidth, bestRiver};
    }

    /**
     * Versión SIN optimización 3 (para comparar)
     */
    public static int[] findOptimalWidthAndRiverNoJumps(String text) {
        String[] words = text.split(" ");
        int minWidth = 0;
        for (String word : words) {
            minWidth = Math.max(minWidth, word.length());
        }
        
        int totalLength = text.length();
        int maxWidth = Math.min(totalLength - 1, 500);
        
        if (maxWidth < minWidth) {
            return new int[]{minWidth, 0};
        }
        
        int bestWidth = minWidth;
        int bestRiver = 0;
        int iterationsCount = 0;
        
        for (int width = minWidth; width <= maxWidth; width++) {
            String[] lines = textToLines(text, width);
            
            if (lines == null || lines.length < 2) {
                continue;
            }
            
            if (lines.length >= bestRiver) {
                iterationsCount++;
                int river = findLongestRiverOptimized(lines);
                
                if (river > bestRiver) {
                    bestRiver = river;
                    bestWidth = width;
                }
                
                if (river == lines.length) {
                    break;
                }
            }
        }
        
        System.err.println("Iteraciones sin saltos: " + iterationsCount);
        return new int[]{bestWidth, bestRiver};
    }

    /**
     * Imprime la matriz de forma visual
     */
    public static void printMatrix(String[][] matriz) {
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                if (matriz[i][j].equals(" ")) {
                    System.out.print("*");
                } else {
                    System.out.print(matriz[i][j]);
                }
            }
            System.out.println();
        }
    }

    public static String[][] textToMatrix(String texto, int anchoLinea) {
        String[] lineas = textToLines(texto, anchoLinea);
        if (lineas == null) return null;
        
        String[][] matriz = new String[lineas.length][];
        for (int i = 0; i < lineas.length; i++) {
            String linea = lineas[i];
            matriz[i] = new String[linea.length()];
            for (int j = 0; j < linea.length(); j++) {
                matriz[i][j] = String.valueOf(linea.charAt(j));
            }
        }
        return matriz;
    }

    public static void main(String[] args) {
        String[] testFiles = {
            "inputSmall.txt",
            "inputSmall2.txt",
            "inputSmall3.txt",
            "input500.txt",
            "input1500.txt",
            "input5000.txt",
            "input10000.txt"
        };
        
        for (String fileName : testFiles) {
            try {
                System.out.println("========================================");
                System.out.println("Probando: " + fileName);
                System.out.println("========================================");
                
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.FileReader(fileName)
                );
                
                String texto = reader.readLine();
                reader.close();
                
                if (texto == null || texto.isEmpty()) {
                    System.out.println("Archivo vacío o no encontrado");
                    continue;
                }
                
                System.out.println("Longitud del texto: " + texto.length() + " caracteres");
                
                // ========== PRUEBA SIN SALTOS ==========
                System.out.println("\n--- SIN Optimización 3 ---");
                long startTime1 = System.currentTimeMillis();
                int[] result1 = findOptimalWidthAndRiverNoJumps(texto);
                long endTime1 = System.currentTimeMillis();
                
                System.out.println("Ancho óptimo: " + result1[0]);
                System.out.println("Río más largo: " + result1[1]);
                System.out.println("Tiempo: " + (endTime1 - startTime1) + " ms");
                
                // ========== PRUEBA CON SALTOS ==========
                System.out.println("\n--- CON Optimización 3 (saltos) ---");
                long startTime2 = System.currentTimeMillis();
                int[] result2 = findOptimalWidthAndRiverWithJumps(texto);
                long endTime2 = System.currentTimeMillis();
                
                System.out.println("Ancho óptimo: " + result2[0]);
                System.out.println("Río más largo: " + result2[1]);
                System.out.println("Tiempo: " + (endTime2 - startTime2) + " ms");
                
                // ========== COMPARACIÓN ==========
                System.out.println("\n--- Comparación ---");
                long diff = (endTime1 - startTime1) - (endTime2 - startTime2);
                double speedup = (double)(endTime1 - startTime1) / (endTime2 - startTime2);
                
                System.out.println("Diferencia: " + diff + " ms");
                System.out.println("Speedup: " + String.format("%.2fx", speedup));
                
                if (result1[0] != result2[0] || result1[1] != result2[1]) {
                    System.out.println("⚠️ ADVERTENCIA: Resultados diferentes!");
                } else {
                    System.out.println("✅ Resultados idénticos");
                }
                
                // Visualización para archivos pequeños
                if (texto.length() < 200) {
                    System.out.println("\nVisualización con ancho óptimo:");
                    String[][] matriz = textToMatrix(texto, result1[0]);
                    printMatrix(matriz);
                }
                
                System.out.println();
                
            } catch (java.io.FileNotFoundException e) {
                System.out.println("Archivo no encontrado: " + fileName);
            } catch (java.io.IOException e) {
                System.out.println("Error leyendo archivo: " + fileName);
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Error procesando: " + fileName);
                e.printStackTrace();
            }
        }
    }
}