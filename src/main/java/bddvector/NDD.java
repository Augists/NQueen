package bddvector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jdd.bdd.BDD;

public class NDD {
    public static int fieldNum;
    public static int[] upperBound;
    public static double[] div;

    public static NDD NDDTrue = new NDD(true);
    public static NDD NDDFalse = new NDD(false);
    public static BDD bdd;

    public static boolean test = false;
    public static long ANDTime = 0L;
    public static long ORTime = 0L;
    public static long NOTTime = 0L;
    public static long ExistTime = 0L;
    public static long ToNDDTime = 0L;

    public Set<ArrayList<Integer>> vectors;

    public static void SetFieldNum(int num) {
        fieldNum = num;

        // lazy init for NDD True and False
        for (ArrayList<Integer> vector : NDDTrue.vectors) {
            for (int i = 0; i < fieldNum; i++) {
                vector.add(1);
            }
        }
        // for (ArrayList<Integer> vector : NDDFalse.vectors) {
        // for (int i = 1; i < fieldNum; i++) {
        // vector.add(1);
        // }
        // vector.add(0);
        // }
    }

    public static void SetUpperBound(int[] upper) {
        // System.out.println("===========set upper bound============");
        // System.out.println("upper bound");
        // for (int i = 0; i < upper.length; i++) {
        //     System.out.print(upper[i] + " ");
        // }
        // System.out.println();

        upperBound = upper;
        int varNum = 0;
        boolean useToBDD = true;
        if (useToBDD) {
            varNum = upper[fieldNum - 1] + 1;
        } else {
            varNum = upper[0] + 1;
            for (int i = 1; i < fieldNum; i++) {
                if (upper[i] - upper[i - 1] > varNum)
                    varNum = upper[i] - upper[i - 1];
            }
        }
        // System.out.println("varNum " + varNum);
        div = new double[fieldNum];
        div[0] = Math.pow(2.0, varNum - upper[0] - 1);
        for (int i = 1; i < fieldNum; i++) {
            div[i] = Math.pow(2.0, varNum - (upper[i] - upper[i - 1]));
        }

        // System.out.println("div");
        // for (int i = 0; i < div.length; i++) {
        //     System.out.print(div[i] + " ");
        // }
        // System.out.println();
    }

    public NDD() {
        vectors = new HashSet<>();
    }

    public NDD(boolean flag) {
        // field = fieldNum + 1;
        vectors = new HashSet<>();
        ArrayList<Integer> vector;
        if (flag) {
            vector = new ArrayList<Integer>();
        } else {
            vector = new ArrayList<Integer>();
        }
        vectors.add(vector);
    }

    public NDD(Set<ArrayList<Integer>> ret) {
        vectors = ret;
    }

    public NDD(NDD ret) {
        this.vectors = new HashSet<>();
        for (ArrayList<Integer> vector : ret.vectors) {
            ArrayList<Integer> newVector = new ArrayList<>(vector);
            this.vectors.add(newVector);
        }
    }

    public boolean equals(NDD obj) {
        return this == obj;
    }

    public boolean is_True() {
        return this == NDDTrue;
    }

    public boolean is_False() {
        return this == NDDFalse;
    }

    public static NDD ref(NDD ndd) {
        return ndd;
    }

    public static void deref(NDD ndd) {
    }

    public static NDD andTo(NDD a, NDD b) {
        NDD t = ref(AND(a, b));
        deref(a);
        return t;
    }

    public static NDD orTo(NDD a, NDD b) {
        NDD t = ref(OR(a, b));
        deref(a);
        return t;
    }

    public static boolean checkCorrectness = false;

    public static NDD AND(NDD a, NDD b) {
        if (test) {
            Long t0 = System.nanoTime();
            NDD ret = ANDRec(a, b);
            Long t1 = System.nanoTime();
            ANDTime += t1 - t0;
            return ret;
        }
        NDD ret = ANDRec(a, b);
        return ret;
    }

    private static NDD ANDRec(NDD a, NDD b) {
        if (a.is_False() || b.is_False())
            return NDDFalse;
        if (a.is_True())
            return b;
        if (b.is_True())
            return a;
        if (a == b)
            return a;
        NDD ret = new NDD();
        for (ArrayList<Integer> vectorA : a.vectors) {
            for (ArrayList<Integer> vectorB : b.vectors) {
                ArrayList<Integer> vectorC = new ArrayList<>();
                // remove vector with any idx pointed to bdd false
                boolean flag = false;
                for (int i = 0; i < vectorA.size(); i++) {
                    int idx = bdd.ref(bdd.and(vectorA.get(i), vectorB.get(i)));
                    if (idx == 0) {
                        flag = true;
                    }
                    vectorC.add(idx);
                }
                if (flag) {
                    for (int i = 0; i < vectorC.size(); i++) {
                        bdd.deref(vectorC.get(i));
                    }
                } else {
                    ret.vectors.add(vectorC);
                }
            }
        }
        return ret;
    }

    public static NDD OR(NDD a, NDD b) {
        if (test) {
            Long t0 = System.nanoTime();
            NDD ret = ORRec(a, b);
            Long t1 = System.nanoTime();
            ORTime += t1 - t0;
            return ret;
        }
        return ORRec(a, b);
    }

    private static NDD ORRec(NDD a, NDD b) {
        if (a.is_True() || b.is_True())
            return NDDTrue;
        if (a.is_False())
            return b;
        if (b.is_False())
            return a;
        if (a == b)
            return a;
        NDD ret = new NDD();
        ret.vectors.addAll(a.vectors);
        ret.vectors.addAll(b.vectors);
        return ret;
    }

    public static NDD Not(NDD a) {
        if (test) {
            Long t0 = System.nanoTime();
            NDD ret = NotRec(a);
            Long t1 = System.nanoTime();
            NOTTime += t1 - t0;
            return ret;
        }
        return NotRec(a);
    }

    private static NDD NotRec(NDD a) {
        if (a.is_True())
            return NDDFalse;
        if (a.is_False())
            return NDDTrue;
        NDD ret = new NDD();
        for (ArrayList<Integer> vector : a.vectors) {
            for (int i = 0; i < vector.size(); i++) {
                ArrayList<Integer> temp = new ArrayList<>(vector); // not 1 1 1 1
                temp.set(i, bdd.not(vector.get(i)));
                ret.vectors.add(temp);
            }
        }
        return ret;
    }

    public static NDD Diff(NDD a, NDD b) {
        NDD n = Not(b);
        NDD ret = AND(a, n);

        return ret;
    }

    public static NDD Exist(NDD a, int field) {
        if (test) {
            Long t0 = System.nanoTime();
            NDD ret = ExistRec(a, field);
            Long t1 = System.nanoTime();
            ExistTime += t1 - t0;
            return ret;
        }

        return ExistRec(a, field);
    }

    private static NDD ExistRec(NDD a, int field) {
        if (a.is_True() || a.is_False())
            return a;
        NDD ret = new NDD(a);
        for (ArrayList<Integer> vector : ret.vectors) {
            vector.set(field, 1);
        }
        return ret;
        // for (ArrayList<Integer> vector : a.vectors) {
        // Set<ArrayList<Integer>> vectorSet = new HashSet<>();
        // for (int i = 0; i < field; i++) {
        // vector.set(i, 0);
        // }
        // vectorSet.add(vector);
        // NDD temp = new NDD(vectorSet);
        // ret = NDD.ORRec(ret, temp);
        // }
        // NDD temp = new NDD(a);
        // for (int i = field; i < fieldNum; i++) {
        // for (ArrayList<Integer> vector : temp.vectors) {
        // vector.set(i, 0);
        // }
        // }
        // return NDD.ORRec(ret, temp);
    }

    private static int bdd_getField(int a) {
        int va = bdd.getVar(a);
        if (a == 1 || a == 0)
            return fieldNum;
        int curr = 0;
        while (curr < fieldNum) {
            if (va <= upperBound[curr]) {
                break;
            }
            curr++;
        }
        return curr;
    }

    public static int toBDD(NDD n) {
        ArrayList<Integer> bdds = new ArrayList<>();
        for (ArrayList<Integer> vector : n.vectors) {
            int lastIdx = 1;
            for (int i = vector.size() - 1; i >= 0; i--) {
                int idx = vector.get(i);
                int temp = lastIdx;
                lastIdx = bdd.and(idx, lastIdx);
                // System.out.println("last idx " + lastIdx);
                // bdd.printDot("/data/augists/NQueen/lastidx"+lastIdx, lastIdx);
                bdd.ref(lastIdx);
                bdd.deref(temp);
            }
            bdds.add(lastIdx);
        }

        int ret = 0;
        for (int i = 0; i < bdds.size(); i++) {
            ret = bdd.or(ret, bdds.get(i));
        }
        return ret;
    }

    public static NDD toNDD(int a) {
        if (test) {
            Long t0 = System.nanoTime();
            NDD ret = toNDDFunc(a);
            Long t1 = System.nanoTime();
            ToNDDTime += t1 - t0;
            return ret;
        }
        return toNDDFunc(a);
    }

    private static NDD toNDDFunc(int a) {
        // if (satCountTest) {
        // System.out.println("=======to bdd vector=======");
        // // System.out.println("root " + a);
        // }

        HashMap<Integer, HashMap<Integer, Integer>> decomposed = decompose(a);

        // System.out.println("decompose" + decomposed);
        // for (Map.Entry<Integer, HashMap<Integer, Integer>> entry :
        // decomposed.entrySet()) {
        // for (Map.Entry<Integer, Integer> i : entry.getValue().entrySet()) {
        // bdd.printDot("/data/augists/NQueen/idx_"+i.getValue(), i.getValue());
        // }
        // }

        ArrayList<Integer> temp = new ArrayList<>();
        Set<ArrayList<Integer>> vectors = new HashSet<>();

        for (int i = 0; i < bdd_getField(a); i++) {
            temp.add(1);
        }

        toBDDvectorDFS(vectors, decomposed, a, temp);

        // System.out.println("vectors " + vectors);

        return new NDD(vectors);
    }

    private static void toBDDvectorDFS(Set<ArrayList<Integer>> vectors,
            HashMap<Integer, HashMap<Integer, Integer>> data, int root, ArrayList<Integer> vector) {
        if (root == 1) {
            ArrayList<Integer> vectorRet = new ArrayList<>(vector);
            vectors.add(vectorRet);
            return;
        }
        HashMap<Integer, Integer> map = data.get(root);
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            int end = entry.getKey();

            // System.out.println("end level " + bdd_getField(end));

            int idx = entry.getValue();
            int fieldDiff = bdd_getField(end) - bdd_getField(root) - 1;

            if (end == 1) {
                ArrayList<Integer> vectorRet = new ArrayList<>(vector);
                vectorRet.add(idx);
                for (int i = 0; i < fieldDiff; i++) {
                    vectorRet.add(1);
                }
                vectors.add(vectorRet);
                continue;
            }

            vector.add(idx);

            for (int i = 0; i < fieldDiff; i++) {
                vector.add(1);
            }

            toBDDvectorDFS(vectors, data, end, vector);

            for (int i = 0; i <= fieldDiff; i++) {
                vector.remove(vector.size() - 1);
            }
        }
    }

    private static HashMap<Integer, HashMap<Integer, Integer>> decompose(int a) {
        HashMap<Integer, HashMap<Integer, Integer>> decomposed_bdd = new HashMap<Integer, HashMap<Integer, Integer>>();
        if (a == 0)
            return decomposed_bdd;
        if (a == 1) {
            HashMap<Integer, Integer> map = new HashMap<>();
            map.put(1, 1);
            decomposed_bdd.put(1, map);
            return decomposed_bdd;
        }
        HashMap<Integer, HashSet<Integer>> boundary_tree = new HashMap<Integer, HashSet<Integer>>();
        ArrayList<HashSet<Integer>> boundary_points = new ArrayList<HashSet<Integer>>();

        get_boundary_tree(a, boundary_tree, boundary_points);
        // System.out.println("boundary tree " + boundary_tree);
        // System.out.println("boundary points " + boundary_points);

        for (int curr_level = 0; curr_level < fieldNum - 1; curr_level++) {
            for (int root : boundary_points.get(curr_level)) {
                for (int end_point : boundary_tree.get(root)) {
                    int res = construct_decomposed_bdd(root, end_point, root);
                    bdd.ref(res);
                    if (!decomposed_bdd.containsKey(root)) {
                        decomposed_bdd.put(root, new HashMap<Integer, Integer>());
                    }
                    decomposed_bdd.get(root).put(end_point, res);
                }
            }
        }

        for (int abdd : boundary_points.get(fieldNum - 1)) {
            if (!decomposed_bdd.containsKey(abdd)) {
                decomposed_bdd.put(abdd, new HashMap<Integer, Integer>());
            }
            decomposed_bdd.get(abdd).put(1, bdd.ref(abdd));
        }

        return decomposed_bdd;
    }

    private static void get_boundary_tree(int a, HashMap<Integer, HashSet<Integer>> boundary_tree,
            ArrayList<HashSet<Integer>> boundary_points) {
        int start_level;
        start_level = bdd_getField(a);
        // System.out.println("start level " + start_level);
        for (int curr = 0; curr < fieldNum; curr++) {
            boundary_points.add(new HashSet<Integer>());
        }
        boundary_points.get(start_level).add(a);
        if (start_level == fieldNum - 1) {
            boundary_tree.put(a, new HashSet<Integer>());
            boundary_tree.get(a).add(1);
            return;
        }

        for (int curr_level = start_level; curr_level < fieldNum; curr_level++) {
            for (int abdd : boundary_points.get(curr_level)) {
                detect_boundary_point(abdd, abdd, boundary_tree, boundary_points);
            }
        }
    }

    private static void detect_boundary_point(int root, int curr,
            HashMap<Integer, HashSet<Integer>> boundary_tree,
            ArrayList<HashSet<Integer>> boundary_points) {
        if (curr == 0)
            return;
        if (curr == 1) {
            if (!boundary_tree.containsKey(root)) {
                boundary_tree.put(root, new HashSet<Integer>());
            }
            boundary_tree.get(root).add(1);
            return;
        }
        if (bdd_getField(root) != bdd_getField(curr)) {
            if (!boundary_tree.containsKey(root)) {
                boundary_tree.put(root, new HashSet<Integer>());
            }
            boundary_tree.get(root).add(curr);
            boundary_points.get(bdd_getField(curr)).add(curr);
            return;
        }
        detect_boundary_point(root, bdd.getLow(curr), boundary_tree,
                boundary_points);
        detect_boundary_point(root, bdd.getHigh(curr), boundary_tree,
                boundary_points);
    }

    private static int construct_decomposed_bdd(int root, int end_point, int curr) {
        if (curr == 0) {
            return curr;
        } else if (curr == 1) {
            if (end_point == 1)
                return 1;
            else
                return 0;
        } else if (bdd_getField(root) != bdd_getField(curr)) {
            if (end_point == curr)
                return 1;
            else
                return 0;
        }

        int low = bdd.getLow(curr);
        int high = bdd.getHigh(curr);

        int new_low = construct_decomposed_bdd(root, end_point, low);
        bdd.ref(new_low);
        int new_high = construct_decomposed_bdd(root, end_point, high);
        bdd.ref(new_high);

        // int field = bdd_getField(curr);
        // int result = 0;
        // if (field == 0) {
        // result = bdd.mk(bdd.getVar(curr), new_low, new_high);
        // } else {
        // result = bdd.mk(bdd.getVar(curr) - upperBound[field - 1] - 1, new_low,
        // new_high);
        // }
        int result = bdd.mk(bdd.getVar(curr), new_low, new_high);
        bdd.deref(new_low);
        bdd.deref(new_high);
        return result;
    }

    public static int toZero(NDD n) {
        int root = NDD.toBDD(n);
        return bdd.toZero(root);
    }

    public static NDD encodeAtMostKFailureVarsSorted(BDD bdd, int[] vars, int startField, int endField, int k) {
        return encodeAtMostKFailureVarsSortedRec(bdd, vars, endField, startField, k);
    }

    private static NDD encodeAtMostKFailureVarsSortedRec(BDD bdd, int[] vars, int endField, int currField, int k) {
        if (currField > endField)
            return NDDTrue;
        int fieldSize = upperBound[0] + 1;
        if (currField > 0)
            fieldSize = upperBound[currField] - upperBound[currField - 1];
        HashMap<NDD, Integer> map = new HashMap<NDD, Integer>();
        for (int i = 0; i <= k; i++) {
            // bdd with k and only k failures
            int pred = bdd.ref(encodeBDD(bdd, vars, fieldSize - 1, 0, i));
            NDD next = encodeAtMostKFailureVarsSortedRec(bdd, vars, endField, currField + 1, k - i);
            int nextPred = 0;
            if (map.containsKey(next))
                nextPred = map.get(next);
            bdd.ref(pred);
            int t = bdd.ref(bdd.or(pred, nextPred));
            bdd.deref(pred);
            bdd.deref(nextPred);
            nextPred = t;
            map.put(next, nextPred);
        }
        return NDD.addAtField(currField, map);
        // return NDD.table.mk(currField, map);
    }

    // replacement of NDD.table.mk(currField, map)
    public static NDD addAtField(int field, HashMap<NDD, Integer> map) {
        NDD ret = new NDD();
        for (Map.Entry<NDD, Integer> entry : map.entrySet()) {
            NDD n = entry.getKey();
            int b = entry.getValue();
            for (ArrayList<Integer> vector : n.vectors) {
                vector.set(field, b);
                ret.vectors.add(vector);
            }
        }
        return ret;
    }

    private static int encodeBDD(BDD bdd, int[] vars, int endVar, int currVar, int k) {
        // cache? link num, k -> bdd
        if (k < 0) {
            return 0;
        }
        if (currVar > endVar) {
            if (k > 0)
                return 0;
            else
                return 1;
        }
        int low = encodeBDD(bdd, vars, endVar, currVar + 1, k - 1);
        int high = encodeBDD(bdd, vars, endVar, currVar + 1, k);
        // return bdd.mk(currVar, low, high);
        return bdd.mk(bdd.getVar(vars[endVar - currVar]), low, high);
    }

    public static double satCount(NDD curr) {
        return satCountRec(curr);
    }

    private static double satCountRec(NDD curr) {
        // if (satCountTest) {
        // System.out.println("===========sat count==============");
        // System.out.println("fieldNum: " + fieldNum);
        // System.out.println("div");
        // for (int i = 0; i < fieldNum; i++) {
        // System.out.print(div[i] + " ");
        // }
        // System.out.println();
        // // System.out.println("vectors: " + curr.vectors);
        // }
        double ret = 0.0;
        ArrayList<Double> retList = new ArrayList<>();
        for (ArrayList<Integer> vector : curr.vectors) {
            double subRet = 1.0;
            for (int i = fieldNum - 1; i >= 0; i--) {
                subRet = subRet * bdd.satCount(vector.get(i)) / div[i];
            }
            // System.out.println("subRet: " + subRet);
            retList.add(subRet);
        }
        for (double count : retList) {
            ret += count;
        }
        return ret;
    }

    public static void nodeCount(NDD node) {
        HashSet<Integer> BDDRootSet = new HashSet<Integer>();
        HashSet<Integer> BDDSet = new HashSet<Integer>();
        for (ArrayList<Integer> vector : node.vectors) {
            for (int i = 0; i < vector.size(); i++) {
                BDDRootSet.add(vector.get(i));
            }
        }
        for (int BDDRoot : BDDRootSet) {
            detectBDD(BDDRoot, BDDSet);
        }
        System.out.println("NDD node:" + node.vectors.size() + " BDD node:" +
                BDDSet.size());
    }

    private static void detectBDD(int node, HashSet<Integer> BDDSet) {
        if (node == 1 || node == 0)
            return;
        else {
            if (!BDDSet.contains(node)) {
                BDDSet.add(node);
                detectBDD(bdd.getHigh(node), BDDSet);
                detectBDD(bdd.getLow(node), BDDSet);
            }
        }
    }

    public static void showStatestic() {
        System.out.println("And:" + ANDTime / 1000000000.0);
        System.out.println("Or:" + ORTime / 1000000000.0);
        System.out.println("Not:" + NOTTime / 1000000000.0);
        System.out.println("Exist:" + ExistTime / 1000000000.0);
        System.out.println("ToNDD:" + ToNDDTime / 1000000000.0);
    }

    // public static void showMemoryUsage() {
    // System.out.println("NDD memory usage:" +
    // (ObjectSizeCalculator.getObjectSize(bdd) / 8 / 1024 / 1024) + "MB");
    // }
}