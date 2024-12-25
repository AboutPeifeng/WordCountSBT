# 使用IntelliJ IDEA创建sbt框架Scala项目实现WordCount计算并打包jar运行

# 前言

此教程写于2024年11月，初衷在于笔者最近有使用Spark的需求，开始上手学习简单的使用。但由于各种软件版本迭代更新太快，以及现有的参考资料较少，完成一个很简单的应用也花费了不少时间、踩了不少坑。最终完成后，特此撰写此教程，与大家分享操作步骤，由于笔者水平有限，文中如有偏颇之处欢迎各位朋友指出交流。

操作在Windows11上进行，实测Ubuntu20.04.2中也行，只是需要注意选择对应系统版本的工具安装

教程中使用的各工具如下（已提前安装完成，且环境变量配置完成）：

java openjdk 1.8.0_432-432

hadoop 3.3.6

scala 2.13.8

spark 3.5.0

sbt 1.8.3

# 在IntelliJ中创建scala项目

1. 打开**IntelliJ**并选择**New Project**，在左侧面板中选择**Scala**
2. 在右侧面板**Name**填写“ScalaProjectDemo”、**Location**自定义（全英无空格路径）
3. 在**Build system**处选择sbt
4. **JDK**选择系统上安装的java，**sbt**版本选择1.8.3，**scala**版本选择2.13.8（此处将**Download sources**勾选，这会为项目下载jar依赖项）
5. 点击**Create**，创建好之后在项目根目录下将会出现如下文件结构：

```python
- ScalaProjectDemo
	- .bsp
	- .idea
	- project (plugins and additional settings for sbt)
	- src (source files)
		- main (application code)
			-scala (Scala source files) <-- This is all we need for now
		- test
	- target (generated files)
	- build.sbt (build definition file for sbt)
```

# 准备数据文件

1. 在左侧**Project**面板上，即项目根目录下，新建data文件夹
2. 在data文件内添加文本文件TextFile.txt，其内容为一些英文句子，为了后续统计次数使用

（可以使用scala或其他工具安装处里的LICENSE.txt文件，里面有丰富的英文句子）

# 编写Scala代码

1. 在左侧Project面板上，找到**ScalaProjectDemo**—>**src**—>**main**
2. 右键单击Scala，选择**New**—>**Package**
3. 将包命名为“example”，按下Enter确定
4. 左侧面板右键单击example，选择**New**—>**Scala Class**，命名为“RunWordCount”，并将”Class“更改为”Object“，按下Enter确定
5. 编写Scala代码：

```scala
package example

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

object RunWordCount extends App {
  // Hide log information
  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("com").setLevel(Level.OFF)
  System.setProperty("spark.ui.showConsoleProgress","false")
  Logger.getRootLogger.setLevel(Level.OFF)

  println("Running WordCount")
  val sc = new SparkContext (new SparkConf().setAppName("wordCount").setMaster("local[4]"))
  println("Reading file")
  val textFile = sc.textFile("data/TextFile.txt")
  println("Creating RDD")
  val countsRDD = textFile.flatMap(line => line.split(" "))
    .map(word => (word, 1))
    .reduceByKey(_ + _)
  println("Saving file")
  try {
    countsRDD.saveAsTextFile("data/output")
    println("Saved successfully")
  }  catch {
    case e: Exception => println("The output file already exists, clear the contents of the output directory");
  }
  println("Task done")
}

```

1. 注意这个时候代码中的org.apache相关部分会报红
2. 找到编译器中**File**—>**Project Structure**—>**Libraries**
3. 点击**+**，选择**Java**，找到Spark安装目录下的jars文件夹并确认
4. 点击**Apply**—>**OK**

# 添加依赖

1. 在左侧Project面板上，找到根目录下的**project**文件夹
2. 新建plugins.sbt文件，目的是添加assembly插件，让sbt能够打包下项目，文件内容如下：

```scala
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.1.0")
```

1. 配置build.sbt文件内容如下：

```scala
name := "ScalaProjectDemo" // Project Name

version := "1.0"

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.0",
  "org.apache.spark" %% "spark-sql" % "3.5.0"
)

enablePlugins(AssemblyPlugin)

assembly / assemblyMergeStrategy := {
  case PathList("example", xs @ _*) => MergeStrategy.first
  case x => MergeStrategy.defaultMergeStrategy(x)
}
```

# 运行项目

有两种方法，第一种是直接在IntelliJ中运行，点击**Run Current File**即可，程序结果会在控制台（终端）输出。第二种是只用sbt工具运行：

1. 在**Run**菜单中，选择**Edit Configurations**
2. 点击**+**，选择**sbt Task**
3. **Name**填写”Run the ScalaFile“
4. 在**Tasks**中，键入“~run”。当保存对项目中文件的更改时，~会导致sbt重新构建并重新运行项目
5. 点击**Apply**—>**OK**
6. 在**Run**菜单上，点击**Run “Run the ScalaFile”**
7. 首次运行sbt会初始化较长时间，等待即可。

运行完程序后，会在项目根目录下data/output目录中产生输出文件。如果输出文件很大，saveAsTextFile命令会自动把它分为多个文件。_SUCCESS代表输出成功，part-00000为数据文件

# 导出jar包

在左侧Tool工具栏面板中，点击**sbt shell**，待sbt初始化完成后，输入assembly即可导出.jar文件，位于根目录下的target/scala-2.13/ScalaProjectDemo_2.13-1.0.jar

现在可以在根目录文件夹下打开终端运行程序，输入以下命令：

```scala
java -jar target\scala-2.13\wordcountsbt_2.13-1.0.jar
```

或者使用spark-submit指令运行：

```scala
spark-submit --class example.RunWordCount --master local[*] target\scala-2.13\wordcountsbt_2.13-1.0.jar
```

Ending