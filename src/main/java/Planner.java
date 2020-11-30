import java.io.*;
import java.util.*;
/**
 * Класс Planner для упорядочивания зависимостей задач.
 * Принимает в main аргументом имя файла, откуда берет данные.
 * Проверки на правильное содержание файла отсутствуют.
 */
public class Planner {
    /**
     * Основное хранилище для данных. Содержит пары "номер задачи - данные о зависимостых".
     * Для хранения данных о зависимостях используется {@link TaskContainer}
     */
    private final HashMap<Integer, TaskContainer> tasks = new HashMap<>();

    public static void main(String[] args){
        long startTime = System.currentTimeMillis();
        try{
            System.out.println("The Data file's name: "+args[0]);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        ArrayList<ArrayList<Integer>> result = new Planner().planTasks(args[0]);
        long endTime = System.currentTimeMillis();
        System.out.println("Work is done.\nExecution time: "+((endTime-startTime)/1000)+"s");
        System.out.println("Start output...");
        Planner.outputData(result);
        System.out.println("Planner has saved result in outputData.txt");
    }
    /**
     * Берет данные из файла и обрабатывает их, сортируя задачи по количеству зависимостей.
     * Для считывания данных использует {@link Planner#readData(String)}
     * @param filename - имя файла с исходными задачами
     * @return Возвращает сортированный список списков задач,
     * в котором решение каждого списка дает возможность решать следующий список.
     */
    public ArrayList<ArrayList<Integer>> planTasks(String filename) {
        //Первым делом считываем файл с данными. По условиям задачи проверять его не требуется.
        readData(filename);
        //Заводим переменную для обработанных данных, которую будет возвращать метод.
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        //Будем учитывать количество задач и количество обработанных задач, а так же номер итерации цикла.
        int tasksCount = tasks.size();
        int checkedTasksCount = 0;
        int actualStepNumber = 0;
        while (tasksCount != checkedTasksCount) {
            //Нам нужно сохранять задачи, которые зависят только от обработанных на предыдущих итерациях (ни от одной
            //другой для первой итерации).
            ArrayList<Integer> actualList = new ArrayList<>();
            //прервем цикл, если на очередном шаге не было обработано ни одной задачи,
            //это говорит о наличии циклических зависимостей.
            boolean hasAnyIndependentTasks = false;
            //Обходим множество задач
            for (Iterator<Map.Entry<Integer, TaskContainer>> iterator = tasks.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<Integer, TaskContainer> entry = iterator.next();
                Integer key = entry.getKey();
                TaskContainer value = entry.getValue();
                //Если нет необходимых для этой задачи задач, добавляем в список независимых на данной итерации
                //(если только она не стала "независимой" на этой же итерации)
                if (value.mainsCount == 0 && value.stepNumber < actualStepNumber) {
                    actualList.add(key);
                    checkedTasksCount++;
                    hasAnyIndependentTasks = true;
                    //Обходим все зависимые задачи,
                    //снижаем для них счетчик зависимостей,
                    //запоминаем номер итерации чтобы не обработать задачи как "независимые" в этой же итерации
                    for (Integer dependantNumber : value.dependants) {
                        TaskContainer taskCont = tasks.get(dependantNumber);
                        taskCont.mainsCount -= 1;
                        taskCont.stepNumber = actualStepNumber;
                    }
                    iterator.remove();
                }
            }
            //Сохраняем список, добавляя в результирующий список списков, и переходим к следующей итерации.
            result.add(actualList);
            actualStepNumber++;
            if (!hasAnyIndependentTasks) break;
        }
        return result;
    }

    /**
     * Считывает файл с исходными задачами и сохраняет в переменную {@link Planner#tasks}
     * @param filename - имя файла с исходными задачами
     */
    private void readData(String filename) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                String[] tasksNumbers = line.split(" ");
                Integer mainTask = Integer.parseInt(tasksNumbers[0]);
                Integer dependant = Integer.parseInt(tasksNumbers[1]);
                //Если зависимая задача уже в общем списке, повышаем для неё счетчик зависимостей
                if (tasks.containsKey(dependant))
                    tasks.get(dependant).mainsCount++;//иначе просто кладем в список с одной завимимостью
                else
                    tasks.put(dependant, new TaskContainer(1));

                tasks.putIfAbsent(mainTask, new TaskContainer());
                //если главной задачи еще нет, добавляем, а потом приписываем ей зависимую в её список.
                tasks.get(mainTask).addDependant(dependant);

                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Выводит результат работы в "outputData.txt"
     * @param result - список списков задач, которые можно решать в порядке вывода строк
     */
    static void outputData(ArrayList<ArrayList<Integer>> result) {
        FileWriter writer;
        try {
            writer = new FileWriter("outputData.txt", false);
            for (List<Integer> list : result) {
                writer.write(list.toString());
                writer.append('\n');
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Встроенный класс для хранения списка зависимых и количества главных задач.
     */
    static class TaskContainer{
        /** Счетчик количества необработанных "главных" для этой задачи задач */
        int mainsCount;
        /** Список зависимых задач */
        ArrayList<Integer> dependants = new ArrayList<>();
        /** используется для пометки итерации цикла, на которой задача стала "независимой",
         * чтобы случайно не обработать её на этой же итерации.
         */
        int stepNumber = -1;
        /**
         * Внесение задачи в список зависимых. {@link TaskContainer#dependants}
         */
        void addDependant(Integer dependant){
            this.dependants.add(dependant);
        }
        public TaskContainer(int mainsCount){
            this.mainsCount = mainsCount;
        }
        public TaskContainer(){}
    }
}
