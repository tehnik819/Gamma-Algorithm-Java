import java.util.ArrayList;
import java.util.Collections;

public class Graph {
    private int[][] matrix;
    private int size = 0;

    public Graph(int[][] m) {
        matrix = m.clone();
        size = m.length;
    }

    public Graph(int size) {
        this.size = size;
        matrix = new int[size][size];
    }

    @Override
    public String toString() {
        String res = "";
        for(int i = 0;i < size;i++) {
            for(int j = i;j < size;j++) {
                if(matrix[i][j] == 1) {
                    res += String.valueOf(i) + " <----> " + String.valueOf(j) + "\n";
                }
            }
        }
        return res;
    }

    public void addEdge(int k, int m) {
        matrix[k][m] = matrix[m][k] = 1;
    }

    public boolean containsEdge(int k, int m) {
        return (matrix[k][m] == 1);
    }

    //Поиск простого цикла, используя DFS алгоритм
    private boolean dfsCycle(ArrayList<Integer> result, int[] used, int parent, int v) {
        used[v] = 1;
        for (int i = 0; i < size; i++) {
            if (i == parent)
                continue;
            if (matrix[v][i] == 0)
                continue;
            if (used[i] == 0) {
                result.add(v);
                if (dfsCycle(result, used, v, i))
                {
                    //Цикл найден
                    return true;
                } else {
                    result.remove(result.size() - 1);
                }
            } if (used[i] == 1) {
                result.add(v);
                //Найден цикл
                ArrayList<Integer> cycle = new ArrayList<>();
                //"Выдергиваем" вершины цикла из порядка обхода
                for (int j = 0; j < result.size(); j++) {
                    if (result.get(j) == i) {
                        cycle.addAll(result.subList(j, result.size()));
                        result.clear();
                        result.addAll(cycle);
                        return true;
                    }
                }
                return true;
            }
        }
        used[v] = 2;
        return false;
    }

    public ArrayList<Integer> getCycle() {
        ArrayList<Integer> cycle = new ArrayList<>();
        boolean hasCycle = dfsCycle(cycle, new int[size], -1, 0);
        if (!hasCycle)
            return null;
        else {
            ArrayList<Integer> result = new ArrayList<>();
            for (int v: cycle)
                result.add(v);
            return result;
        }
    }

    //Поиск связных компонент графа G - G', дополненного ребрами из G,
    // один из концов которых принадлежит связной компоненте, а другой G'
    private void dfsSegments(int[] used, boolean[] laidVertexes, Graph result, int v) {
        used[v] = 1;
        for (int i = 0; i < size; i++) {
            if (matrix[v][i] == 1) {
                result.addEdge(v, i);
                if (used[i] == 0 && !laidVertexes[i])
                    dfsSegments(used, laidVertexes, result, i);
            }
        }
    }

    private ArrayList<Graph> getSegments(boolean[] laidVertexes, boolean[][] edges) {
        ArrayList<Graph> segments = new ArrayList<>();
        //Поиск однореберных сегментов
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (matrix[i][j] == 1 && !edges[i][j] && laidVertexes[i] && laidVertexes[j]) {
                    Graph t = new Graph(size);
                    t.addEdge(i, j);
                    segments.add(t);
                }
            }
        }
        //Поиск связных компонент графа G - G', дополненного ребрами из G,
        // один из концов которых принадлежит связной компоненте, а другой G'
        int used[] = new int[size];
        for (int i = 0; i < size; i++) {
            if (used[i] == 0 && !laidVertexes[i]) {
                Graph res = new Graph(size);
                dfsSegments(used, laidVertexes, res, i);
                segments.add(res);
            }
        }
        return segments;
    }

    //Поиск цепи в выбранном сегменте, используя DFS алгоритм
    private void dfsChain(int used[], boolean[] laidVertexes, ArrayList<Integer> chain, int v) {
        used[v] = 1;
        chain.add(v);
        for (int i = 0; i < size; i++) {
            if (matrix[v][i] == 1 && used[i] == 0) {
                if (!laidVertexes[i]) {
                    dfsChain(used, laidVertexes, chain, i);
                } else {
                    chain.add(i);
                }
                return;
            }
        }
    }

    private ArrayList<Integer> getChain(boolean[] laidVertexes) {
        ArrayList<Integer> result = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            if (laidVertexes[i]) {
                boolean inGraph = false;
                for (int j = 0; j < size; j++) {
                    if (containsEdge(i, j))
                        inGraph = true;
                }
                if (inGraph) {
                    dfsChain(new int[size], laidVertexes, result, i);
                    break;
                }
            }
        }
        return result;
    }

    //Укладка цепи, описание матрицы смежности
    public static void layChain(boolean[][] result, ArrayList<Integer> chain, boolean cyclic) {
        for (int i = 0; i < chain.size() - 1; i++) {
            result[chain.get(i)][chain.get(i + 1)] = true;
            result[chain.get(i + 1)][chain.get(i)] = true;
        }
        if (cyclic) {
            result[chain.get(0)][chain.get(chain.size() - 1)] = true;
            result[chain.get(chain.size() - 1)][chain.get(0)] = true;
        }
    }

    //Проверка на то, что данный сегмент содержится в данной грани
    private boolean isFaceContainsSegment(final ArrayList<Integer> face, final Graph segment, boolean[] laidVertexes) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (segment.containsEdge(i, j)) {
                    if ((laidVertexes[i] && !face.contains(i)) || (laidVertexes[j] && !face.contains(j))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //Считаем число граней, вмещающих данные сегмент
    private int[] calcNumOfFacesContainedSegments(ArrayList<ArrayList<Integer>> intFaces, ArrayList<Integer> extFace,
                                                  ArrayList<Graph> segments, boolean[] laidVertexes,ArrayList<Integer>[] destFaces) {
        int[] count = new int[segments.size()];
        for (int i = 0; i < segments.size(); i++) {
            for (ArrayList<Integer> face : intFaces) {
                if (isFaceContainsSegment(face, segments.get(i), laidVertexes)) {
                    destFaces[i] = face;
                    count[i]++;
                }
            }
            if (isFaceContainsSegment(extFace, segments.get(i), laidVertexes)) {
                destFaces[i] = extFace;
                count[i]++;
            }
        }
        return count;
    }

    //Получить плоскую укладку графа
    //Возвращаются все грани уложенного планарного графа
    //Если это невозможно(граф не планарный), то null
    public Faces getPlanarLaying() {
        //Если граф одновершинный, то возвращаем две грани
        if (size == 1) {
            ArrayList<ArrayList<Integer>> faces = new ArrayList<>();
            ArrayList<Integer> outerFace = new ArrayList<>();
            outerFace.add(0);
            faces.add(outerFace);
            faces.add((ArrayList<Integer>) outerFace.clone());
            return new Faces(faces, outerFace);
        }

        //Ищем цикл, если его нет, до граф не соответствует условиям алгоритма
        //(Нет циклов => дерево => планарный)
        ArrayList<Integer> c = getCycle();
        if(c.isEmpty()) {
            return null;
        }
        //Списки граней
        ArrayList<ArrayList<Integer>> intFaces = new ArrayList<>();
        ArrayList<Integer> extFace = (ArrayList<Integer>) c.clone();
        intFaces.add(c);
        intFaces.add(extFace);

        //Массивы уже уложенных вершин и ребер соответственно
        boolean[] laidVertexes = new boolean[size];
        boolean[][] laidEdges = new boolean[size][size];
        for (int i : c) {
            laidVertexes[i] = true;
        }
        //Укладываем найденный цикл
        layChain(laidEdges, c, true);
        //Второй шаг алгоритма:
        //выделение множества сегментов, подсчет числа вмещающих граней,
        //выделение цепей из сегментов, укладка цепей, добавление новых граней
        while (true) {
            ArrayList<Graph> segments = getSegments(laidVertexes, laidEdges);
            //Если нет сегментов, го граф - найденный постой цикл => планарный
            if (segments.size() == 0) {
                break;
            }
            //Массив граней, в которые будут уложены соответствующие сегменты с минимальным числом calcNumOfFacesContainedSegments()
            ArrayList<Integer>[] destFaces = new ArrayList[segments.size()];
            int[] count = calcNumOfFacesContainedSegments(intFaces, extFace, segments,laidVertexes, destFaces);

            //Ищем минимальное число calcNumOfFacesContainedSegments()
            int mi = 0;
            for (int i = 0; i < segments.size(); i++) {
                if (count[i] < count[mi])
                    mi = i;
            }
            //Если хотя бы одно ноль, то граф не планарный
            if (count[mi] == 0) {
                return null;
            } else {
                //Укладка выбранного сегмента
                //Выделяем цепь между двумя контактными вершинами
                ArrayList<Integer> chain = segments.get(mi).getChain(laidVertexes);
                //Помечаем вершины цепи как уложенные
                for (int i : chain) {
                    laidVertexes[i] = true;
                }
                //Укладываем соответствующие ребра цепи
                layChain(laidEdges, chain, false);

                //Целевая грань, куда будет уложен выбранный сегмент
                ArrayList<Integer> face = destFaces[mi];
                //Новые грани, порожденные разбиением грани face выбранным сегментом
                ArrayList<Integer> face1 = new ArrayList<>();
                ArrayList<Integer> face2 = new ArrayList<>();
                //Ищем номера контактных вершин цепи
                int contactFirst = 0, contactSecond = 0;
                for (int i = 0; i < face.size(); i++) {
                    if (face.get(i).equals(chain.get(0))) {
                        contactFirst = i;
                    }
                    if (face.get(i).equals(chain.get(chain.size() - 1))) {
                        contactSecond = i;
                    }
                }
                //Находим обратную цепь(цепь, пробегаемая в обратном направлении)
                ArrayList<Integer> reverseChain = (ArrayList<Integer>) chain.clone();
                Collections.reverse(reverseChain);
                int faceSize = face.size();
                if (face != extFace) { //Если целевая грань не внешняя
                    //Укладываем прямую цепь в одну из порожденных граней,
                    //а обратную в другую в зависимости от номеров контактных вершин
                    if (contactFirst < contactSecond) {
                        face1.addAll(chain);
                        for (int i = (contactSecond + 1) % faceSize; i != contactFirst; i = (i + 1) % faceSize) {
                            face1.add(face.get(i));
                        }
                        face2.addAll(reverseChain);
                        for (int i = (contactFirst + 1) % faceSize; i != contactSecond; i = (i + 1) % faceSize) {
                            face2.add(face.get(i));
                        }
                    } else {
                        face1.addAll(reverseChain);
                        for (int i = (contactFirst + 1) % faceSize; i != contactSecond; i = (i + 1) % faceSize) {
                            face1.add(face.get(i));
                        }
                        face2.addAll(chain);
                        for (int i = (contactSecond + 1) % faceSize; i != contactFirst; i = (i + 1) % faceSize) {
                            face2.add(face.get(i));
                        }
                    }
                    //Удаляем целевую грань(она разбилась на две новые)
                    //Добавляем порожденные грани в множество внутренних граней
                    intFaces.remove(face);
                    intFaces.add(face1);
                    intFaces.add(face2);
                } else { //Если целевая грань совпала с внешней
                    //Все то же самое, только одна из порожденных граней - новая внешняя грань
                    ArrayList<Integer> newOuterFace = new ArrayList<>();
                    if (contactFirst < contactSecond) {
                        newOuterFace.addAll(chain);
                        for (int i = (contactSecond + 1) % faceSize; i != contactFirst; i = (i + 1) % faceSize) {
                            newOuterFace.add(face.get(i));
                        }
                        face2.addAll(chain);
                        for (int i = (contactSecond - 1 + faceSize) % faceSize; i != contactFirst; i = (i - 1 + faceSize) % faceSize) {
                            face2.add(face.get(i));
                        }
                    } else {
                        newOuterFace.addAll(reverseChain);
                        for (int i = (contactFirst + 1) % faceSize; i != contactSecond; i = (i + 1) % faceSize) {
                            newOuterFace.add(face.get(i));
                        }
                        face2.addAll(reverseChain);
                        for (int i = (contactFirst - 1 + faceSize) % faceSize; i != contactSecond; i = (i - 1 + faceSize) % faceSize) {
                            face2.add(face.get(i));
                        }
                    }
                    //Удаляем старые, добавляем новые
                    intFaces.remove(extFace);
                    intFaces.add(newOuterFace);
                    intFaces.add(face2);
                    extFace = newOuterFace;
                }
            }
        }
        return new Faces(intFaces, extFace);
    }
}