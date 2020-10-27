import java.io.*;
import java.util.*;
/**
 * Класс Planner для упорядочивания зависимостей.
 * Принимает в main аргументом имя файла, откуда берет данные.
 * Проверки на правильное содержание файла отсутствуют.
 */
public class Planner {

    public static void main(String[] args){
        long startTime = System.currentTimeMillis();
        try{
            System.out.println("The Data file's name: "+args[0]);
            planTasks(args[0]);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Work is done.\nExecution time: "+((endTime-startTime)/1000)+"s");
    }
    /**
     * Основной метод, делающий всю работу: читает файл, вспомогательным методом проверяет
     * зависимости, выводит результат.
     * @param fileName - имя файла на чтение, вносится аргументом при запуске в main
     */
    private static void planTasks(String fileName) {
        //основное хранилище для отношения зависимости
        HashSet<TaskContainer> allTasks = new HashSet<>();
        //хранилище для главных задач
        HashSet<Integer> mainTasks = new HashSet<>();
        //хранилище для зависимых задач
        HashSet<Integer> dependTasks = new HashSet<>();
        try (FileReader fileReader = new FileReader(fileName)){
            BufferedReader reader = new BufferedReader(fileReader);
            String line=reader.readLine();
            while(line != null) {
                //проходимся по файлу, запоминаем все отношения, а заодно разделяем задачи на главные и зависимые
                TaskContainer singleTask = new TaskContainer(line.split(" "));
                mainTasks.add(singleTask.getMainTask());
                dependTasks.add(singleTask.getDepTask());
                allTasks.add(singleTask);
                line = reader.readLine();
            }
            System.out.println("File's read.");
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        //Подаем данные на обработку и вывод
        processTasks(mainTasks,dependTasks,allTasks);

    }
    /**
     * Берет на себя обработку и одновременный вывод данных.
     * @param mains - подготовленное множество главных задач
     * @param dependants - подготовленное множество зависимых задач
     * @param tasksPairs - зависимости задач
     */
    private static void processTasks(HashSet<Integer> mains, HashSet<Integer> dependants, HashSet<TaskContainer> tasksPairs){
        HashSet<Integer> newMainTasks;
        if (tasksPairs.isEmpty()) return;
        try(FileWriter outputData = new FileWriter("outputData.txt", false))
        {
            do {
                newMainTasks = new HashSet<>();
                //System.out.println("Values: "+valList.toString());
                //вычитаем множество зависимых из множества главных, получая независимые задачи
                mains.removeAll(dependants);
                dependants = new HashSet<>();
                outputData.write(mains.toString());
                outputData.append('\n');
                Iterator<TaskContainer> it = tasksPairs.iterator();
                while(it.hasNext()){
                    //в цикле удаляем из множества отношений те, где главная задача независима, а зависимые от них
                    //помещаем в новое множество главных, чтобы не потерять
                    //заодно снова заполняем множество зависымых задач
                    TaskContainer singleOne = it.next();
                    if(mains.contains(singleOne.getMainTask())){
                        newMainTasks.add(singleOne.getDepTask());
                        it.remove();
                    } else {
                        newMainTasks.add(singleOne.getMainTask());
                        dependants.add(singleOne.getDepTask());
                    }
                }
                //выводим независимые задачи этой итерации
                mains = newMainTasks;
            }while(!mains.isEmpty());
            outputData.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    /**
     * Встроенный класс для хранения пары главной-зависимой задач.
     */
    protected static class TaskContainer{
        /** Пара из двух номеров задач */
        private final Integer[] tasksPair = new Integer[2];
        /**
         * Функция получения первого значения поля {@link TaskContainer#tasksPair}
         * @return возвращает номер главной задачи
         */
        public Integer getMainTask() {
            return tasksPair[0];
        }
        /**
         * Функция получения второго значения поля {@link TaskContainer#tasksPair}
         * @return возвращает номер зависимой задачи
         */
        public Integer getDepTask(){
            return tasksPair[1];
        }
        /**
         * Конструктор, вносящий в {@link TaskContainer#tasksPair} значения из массива из двух строк.
         * @param s - массив из двух кнвертируемых в Integer строк, проверок не производится
         */
        public TaskContainer(String[] s){
            if (s.length == 2) {
                tasksPair[0] = Integer.valueOf(s[0]);
                tasksPair[1] = Integer.valueOf(s[1]);
            }
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TaskContainer taskContainer = (TaskContainer) o;
            return Arrays.equals(tasksPair, taskContainer.tasksPair);
        }
        @Override
        public int hashCode() {
            return Arrays.hashCode(tasksPair);
        }
    }
}
