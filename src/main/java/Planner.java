import java.io.*;
import java.util.*;
/**
 * Класс Planner для упорядочивания зависимостей.
 * Принимает в main аргументом имя файла, откуда берет данные.
 * Проверки на правильное содержание файла отсутствуют.
 */
public class Planner {

    public static void main(String[] args){
        try{
            System.out.println("The Data file's name: "+args[0]);
            long startTime = System.currentTimeMillis();
            planTasks(args[0]);
            long endTime = System.currentTimeMillis();
            System.out.println("Work is done.\nExecution time: "+((endTime-startTime)/1000)+"s");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Основной метод, делающий всю работу: читает файл, проверяет зависимости, выводит результат.
     * @param fileName - имя файла на чтение, вносится аргументом при запуске
     */
    protected static void planTasks(String fileName) {
        //основное хранилище для отношения зависимости
        HashSet<DataCont> allData=new HashSet<>();
        //хранилище для главных задач
        HashSet<Integer> keys=new HashSet<>();
        //хранилище для зависимых задач
        HashSet<Integer> vals = new HashSet<>();
        try (FileReader dataInput = new FileReader(fileName)){
            BufferedReader reader = new BufferedReader(dataInput);
            String line=reader.readLine();
            //long k=0;
            while(line!=null) {
                //проходимся по файлу, запоминаем все отношения, а заодно разделяем задачи на главные и зависимые
                DataCont singleOne= new Planner.DataCont(line.split(" "));
                keys.add(singleOne.takeKey());
                vals.add(singleOne.takeVal());
                allData.add(singleOne);
                line=reader.readLine();
                //System.out.println((k++)+"");
            }
        }
        catch(IOException ex) {
            System.out.println(ex.getMessage());
        }
        //и тут начинается магия с множествами
        HashSet<Integer> nextKeys;
        System.out.println("File's read.");
        if (allData.isEmpty()) return;
        try(FileWriter outputData = new FileWriter("outputData.txt", false))
        {
            //System.out.println("First keys: "+keys.toString());
            //int l=0;
            do {
                nextKeys= new HashSet<>();
                //System.out.println("Values: "+valList.toString());
                //вычитаем множество зависимых из множества главных, получая независимые задачи
                keys.removeAll(vals);
                vals=new HashSet<>();
                outputData.write(keys.toString());
                outputData.append('\n');
                Iterator<DataCont> it = allData.iterator();
                while(it.hasNext()){
                    //в цикле удаляем из множества отношений те, где главная задача независима, а зависимые от них
                    //помещаем в новое множество главных, чтобы не потерять
                    //заодно снова заполняем множество зависымых задач
                    DataCont singleOne=it.next();
                    if(keys.contains(singleOne.takeKey())){
                        nextKeys.add(singleOne.takeVal());
                        it.remove();
                    }
                    else{
                        nextKeys.add(singleOne.takeKey());
                        vals.add(singleOne.takeVal());
                    }
                }
                //выводим независсимые задачи этой итерации
                keys=nextKeys;
                //if((l%5)==0)System.out.println(l+", new keys: "+keys.toString());
                //l++;
            }while(!keys.isEmpty());
            outputData.close();
        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
        }
    }
    /**
     * Встроенный класс для хранения пары главной-зависимой задач.
     */
    protected static class DataCont{
        /** Пара из двух номеров задач */
        protected Integer[] keyVal=new Integer[2];
        /**
         * Функция получения первого значения поля {@link DataCont#keyVal}
         * @return возвращает номер главной задачи
         */
        public Integer takeKey() {
            return keyVal[0];
        }

        /**
         * Функция получения второго значения поля {@link DataCont#keyVal}
         * @return возвращает номер зависимой задачи
         */
        public Integer takeVal(){
            return keyVal[1];
        }
        /**
         * Конструктор, вносящий в {@link DataCont#keyVal} значения из массива из двух строк.
         * @param s - массив из двух кнвертируемых в Integer строк, проверок не производится
         */
        public DataCont(String[] s){
            if (s.length==2) {
                keyVal[0]=Integer.valueOf(s[0]);
                keyVal[1]=Integer.valueOf(s[1]);
            }
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataCont dataCont = (DataCont) o;
            return Arrays.equals(keyVal, dataCont.keyVal);
        }
        @Override
        public int hashCode() {
            return Arrays.hashCode(keyVal);
        }
    }
}
