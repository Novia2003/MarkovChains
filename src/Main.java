import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        BigDecimal[][] matrix = readMatrix();
        List<Character> letters = Arrays.asList('a', 'b', 'c');

        BigDecimal[] p = findProbabilityValues(matrix, letters);

        BigDecimal entropyDistribution = findEntropyDistribution(p);

        BigDecimal jointEntropy = findJointEntropy(matrix, p, letters);

        findConditionalEntropy(matrix, p, entropyDistribution, jointEntropy);
    }

    private static BigDecimal[][] readMatrix() {
        BigDecimal[][] matrix = new BigDecimal[3][3];

        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[r].length; c++)
                matrix[r][c] = scanner.nextBigDecimal();
        }

        return matrix;
    }

    private static BigDecimal[] findProbabilityValues(BigDecimal[][] matrix,  List<Character> letters) {
        BigDecimal[][] a = new BigDecimal[matrix.length][matrix[0].length];
        for (int r = 0; r < matrix.length - 1; r++) {
            for (int c = 0; c < a[r].length; c++) {
                a[r][c] = (r == c) ? matrix[r][c].add(BigDecimal.valueOf(-1)) : matrix[r][c];
            }
        }

        a[matrix.length - 1] = new BigDecimal[]{BigDecimal.valueOf(1), BigDecimal.valueOf(1), BigDecimal.valueOf(1)};

        BigDecimal[] b = new BigDecimal[]{BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(1)};

        int n = a.length;

        for (int k = 0; k < n; k++) {
            for (int j = k + 1; j < n; j++) {
                BigDecimal c = a[j][k].divide(a[k][k], new MathContext(8, RoundingMode.HALF_UP)).stripTrailingZeros();

                for (int i = k; i < n; i++)
                    a[j][i] = a[j][i].add(c.multiply(a[k][i]).negate());

                b[j] = b[j].add(c.multiply(b[k]).negate());
            }
        }

        BigDecimal[] p = new BigDecimal[n];

        for (int k = n - 1; k >= 0; k--) {
            p[k] = b[k];

            for (int j = k + 1; j < n; j++)
                p[k] = p[k].add(a[k][j].multiply(p[j]).negate());

            p[k] = p[k].divide(a[k][k], new MathContext(8, RoundingMode.HALF_UP)).stripTrailingZeros();
        }

        System.out.println("Значения вероятностей стационарного распределения марковской цепи: ");
        for (int i = 0; i < n; i++) {
            System.out.println("p_" + letters.get(i) + " = " + p[i]);
        }

        return p;
    }

    private static BigDecimal findEntropyDistribution(BigDecimal[] probabilities) {
        BigDecimal entropyDistribution = BigDecimal.valueOf(0);

        double ln2 = Math.log(2);

        for (BigDecimal probability : probabilities) {
            double value = probability.doubleValue();
            double ln = Math.log(value);
            entropyDistribution = entropyDistribution.add(probability.multiply(BigDecimal.valueOf(ln / ln2)));
        }

        entropyDistribution = entropyDistribution.negate().stripTrailingZeros();

        System.out.println();
        System.out.println("Энтропия распределения H(X_i): " + entropyDistribution + " бит");

        return entropyDistribution;
    }

    private static BigDecimal findJointEntropy(BigDecimal[][] matrix, BigDecimal[] probabilities, List<Character> letters) {
        BigDecimal jointEntropy = BigDecimal.valueOf(0);

        double ln2 = Math.log(2);

        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[r].length; c++) {
                BigDecimal jointProbability = probabilities[c].multiply(matrix[r][c]);
                System.out.println("p(x_i=" + letters.get(c) + ", x_(i+1)=" + letters.get(r) + ") = " + jointProbability.stripTrailingZeros());
                double value = jointProbability.doubleValue();
                double ln = Math.log(value);
                jointEntropy = jointEntropy.add(jointProbability.multiply(BigDecimal.valueOf(ln / ln2)));
            }
        }

        jointEntropy = jointEntropy.negate().stripTrailingZeros();

        System.out.println();
        System.out.println("Совместную энтропию H(x_i x_(i+1)): " + jointEntropy + " бит");

        return jointEntropy;
    }

    private static void findConditionalEntropy(
            BigDecimal[][] matrix, BigDecimal[] probabilities, BigDecimal entropyDistribution, BigDecimal jointEntropy
    ) {
        BigDecimal conditionalEntropy = BigDecimal.valueOf(0);

        double ln2 = Math.log(2);

        for (int r = 0; r < matrix.length; r++) {
            BigDecimal sum = BigDecimal.valueOf(0);

            for (BigDecimal probability : matrix[r]) {
                double value = probability.doubleValue();
                double ln = Math.log(value);
                sum = sum.add(probability.multiply(BigDecimal.valueOf(ln / ln2)));
            }

            conditionalEntropy = conditionalEntropy.add(probabilities[r].multiply(sum));
        }

        conditionalEntropy = conditionalEntropy.negate().stripTrailingZeros();

        System.out.println();
        System.out.println("Условная энтропия Hx_i (x_(i+1)): ");
        System.out.println("1 вариант: " +  conditionalEntropy + " бит");
        System.out.println("2 вариант: " + jointEntropy.add(entropyDistribution.negate()).stripTrailingZeros() + " бит");
    }
}

/*
0,5 0,6 0,4
0,35 0,2 0,1
0,15 0,2 0,5
 */

/*
0,1 0,1 0,4
0,1 0,3 0,3
0,8 0,6 0,3
 */

/*
0,25 0,09 0,2
0,45 0,64 0,41
0,3 0,27 0,39
 */