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
        int maxWidth = Math.min(totalLength - 1, 5000);        
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
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        
        String[][] testCategories = {
            {"Casos básicos", "inputSmall.txt", "inputSmall2.txt", "inputSmall3.txt"},
            {"CasosDePruebaA-Z", 
            "CasosDePruebaA-Z/a-z500.txt",
            "CasosDePruebaA-Z/a-z1000.txt",
            "CasosDePruebaA-Z/a-z20000.txt",
            "CasosDePruebaA-Z/a-z50000.txt",
            "CasosDePruebaA-Z/a-z100000.txt"
            },
            {"CasosDePruebaCicero",
            "CasosDePruebaCicero/cicero500.txt",
            "CasosDePruebaCicero/cicero1000.txt",
            "CasosDePruebaCicero/cicero20000.txt",
            "CasosDePruebaCicero/cicero50000.txt",
            "CasosDePruebaCicero/cicero100000.txt"
            },
            {"CasosDePruebaLiLanguages",
            "CasosDePruebaLiLanguages/LiLang500.txt",
            "CasosDePruebaLiLanguages/LiLang1000.txt",
            "CasosDePruebaLiLanguages/LiLang20000.txt",
            "CasosDePruebaLiLanguages/LiLang50000.txt",
            "CasosDePruebaLiLanguages/LiLang100000.txt"
            },
            {"CasosDePruebaLoremIpsum",
            "CasosDePruebaLoremIpsum/loremipsum500.txt",
            "CasosDePruebaLoremIpsum/loremipsum1000.txt",
            "CasosDePruebaLoremIpsum/loremipsum20000.txt",
            "CasosDePruebaLoremIpsum/loremipsum50000.txt",
            "CasosDePruebaLoremIpsum/loremipsum100000.txt"
            }
        };
        
        while (true) {
            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║  MENÚ DE CASOS DE PRUEBA - V3 (CON COMPARACIÓN)");
            System.out.println("╚════════════════════════════════════════════════════════╝");
            System.out.println("\n0. Probar TODOS los casos");
            for (int i = 0; i < testCategories.length; i++) {
                System.out.println((i + 1) + ". " + testCategories[i][0]);
            }
            System.out.println((testCategories.length + 1) + ". Salir");
            
            System.out.print("\nSeleccione una opción: ");
            int opcion = scanner.nextInt();
            
            if (opcion == testCategories.length + 1) {
                System.out.println("\n¡Hasta luego!");
                break;
            }
            
            if (opcion == 0) {
                // Probar todos
                for (String[] category : testCategories) {
                    probarCategoriaConComparacion(category);
                }
            } else if (opcion >= 1 && opcion <= testCategories.length) {
                // Probar categoría específica
                probarCategoriaConComparacion(testCategories[opcion - 1]);
            } else {
                System.out.println("⚠️ Opción inválida");
            }
        }
        
        scanner.close();
    }

    // ============================================================
    // Método auxiliar para probar una categoría con comparación
    // ============================================================
    private static void probarCategoriaConComparacion(String[] category) {
        String categoryName = category[0];
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║  " + categoryName);
        System.out.println("╚════════════════════════════════════════════════════════╝");
        
        for (int i = 1; i < category.length; i++) {
            String fileName = category[i];
            try {
                System.out.println("\n--- " + fileName + " ---");
                
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.FileReader(fileName)
                );
                
                String texto = reader.readLine();
                reader.close();
                
                if (texto == null || texto.isEmpty()) {
                    System.out.println("Archivo vacío o no encontrado");
                    continue;
                }
                
                // ========== PRUEBA SIN SALTOS ==========
                long startTime1 = System.currentTimeMillis();
                int[] result1 = findOptimalWidthAndRiverNoJumps(texto);
                long endTime1 = System.currentTimeMillis();
                
                // ========== PRUEBA CON SALTOS ==========
                long startTime2 = System.currentTimeMillis();
                int[] result2 = findOptimalWidthAndRiverWithJumps(texto);
                long endTime2 = System.currentTimeMillis();
                
                // ========== IMPRIMIR RESULTADO (formato V1/V2) ==========
                long diff = (endTime1 - startTime1) - (endTime2 - startTime2);
                double speedup = (endTime2 - startTime2) > 0 ? 
                    (double)(endTime1 - startTime1) / (endTime2 - startTime2) : 0;
                
                System.out.println("Longitud: " + texto.length() + " chars | " +
                                "Ancho: " + result1[0] + " | " +
                                "Río: " + result1[1] + " | " +
                                "Sin Opt3: " + (endTime1 - startTime1) + " ms | " +
                                "Con Opt3: " + (endTime2 - startTime2) + " ms | " +
                                "Speedup: " + String.format("%.2fx", speedup));
                
                // Verificar que ambos métodos den el mismo resultado
                if (result1[0] != result2[0] || result1[1] != result2[1]) {
                    System.out.println("⚠️ ADVERTENCIA: Resultados diferentes!");
                }
                
            } catch (java.io.FileNotFoundException e) {
                System.out.println("⚠️ Archivo no encontrado: " + fileName);
            } catch (Exception e) {
                System.out.println("⚠️ Error: " + fileName);
                e.printStackTrace();
            }
        }
    }
}