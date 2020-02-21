package com.mec.mfct.file;

import com.mec.mfct.progressBar.IProgressBar;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class Receiver implements Runnable{
    private static final int DEFAULT_POOL_CORE_SIZE = 30;
    private static final int DEFAULT_POOL_MAX_SIZE = 50;
    private static final long DEFAULT_POOL_THREAD_ALIVE_TIME = 100;
    private static final TimeUnit DEFAULT_POOL_UNIT = TimeUnit.MILLISECONDS;
    private static final int DEFAULT_QUEUE_SIZE = 20;

    private int poolCoreSize;
    private int poolMaxSize;
    private long poolThreadAliveTime;
    private TimeUnit timeUnit;
    private int queueSize;

    private ServerSocket serverSocket;
    private int port;
    private int connectCount;
    private volatile int workerThreadCount;

    private ThreadPoolExecutor threadPool;
    private volatile Resource resource;
    private IProgressBar progressBar;

    public Receiver() {
        this(0, null);
    }

    public Receiver(int connectCount, Resource resource) {
        this.connectCount = connectCount;
        this.resource = resource;

        poolCoreSize = DEFAULT_POOL_CORE_SIZE;
        poolMaxSize = DEFAULT_POOL_MAX_SIZE;
        poolThreadAliveTime = DEFAULT_POOL_THREAD_ALIVE_TIME;
        timeUnit = DEFAULT_POOL_UNIT;
        queueSize = DEFAULT_QUEUE_SIZE;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public void setConnectCount(int connectCount) {
        this.connectCount = connectCount;
    }

    public void startup() throws Exception {
        if (connectCount <= 0) {
            throw new Exception("没有设置连接个数或者是没有这个资源!!");
        }
        if (port < 1000 || port > 65535) {
            throw  new Exception("端口设置异常!!!");
        }

        BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>(queueSize);
        threadPool = new ThreadPoolExecutor(poolCoreSize, poolMaxSize, poolThreadAliveTime, timeUnit, queue);
        serverSocket = new ServerSocket(port);
        workerThreadCount = connectCount;
        new Thread(this, "Receiver").start();
    }

    @Override
    public void run() {
        int count = 0;
        while (count < connectCount) {
            System.out.println("已连接的count" + count + ":::应该连接的count:::" + connectCount);
            try {
                Socket socket = serverSocket.accept();
                System.out.println("开始创建关键接受线程!!");
                threadPool.execute(new Worker(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
            count++;
        }
        stop();
    }

    public void stop() {
        System.out.println("Receiver已经结束了！！！！");
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            serverSocket = null;
        }
    }

    public void setProgressBar(IProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public IProgressBar getProgressBar() {
        return progressBar;
    }

    class Worker implements Runnable {
        private Socket socket;
        private DataInputStream dis;

        Worker(Socket socket) throws IOException {
            this.socket = socket;
            System.out.println("ip " + socket.getInetAddress().getHostAddress() + "发送端已连接!!");
            dis = new DataInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            try {
                while (true) {
                    byte[] data = new byte[16];
                    dis.readFully(data);
                    MataData metaData = new MataData(data);
                    long offset = metaData.getOffset();
                    int fileId = metaData.getFileId();
                    if (fileId == -1) {
                        workerThreadCount--;
                        if (workerThreadCount == 0) {
                            synchronized (Receiver.class) {
                                Receiver.class.notify();
                            }
                            System.out.println("文件传输结束了！！！！");
                        }
                        break;
                    }
                    int len = metaData.getLength();
                    if (progressBar != null) {
                        progressBar.createProgress(resource.getFileSet().getFile(fileId));
                    }
                    data = new byte[len];
                    dis.readFully(data);
                    resource.receiveSection(fileId, offset, data);
                }
            } catch (IOException e) {
                e.printStackTrace();
                workerThreadCount--;
                if (workerThreadCount == 0) {
                    synchronized (Receiver.class) {
                        Receiver.class.notify();
                    }
                    System.out.println("文件传输结束了！！！！");
                }
                System.out.println("传输失败，尝试再次连接中...");
            }
            stop();
        }

        private void stop() {
            try {
                dis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void initThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int queueSize) {
        this.poolCoreSize = corePoolSize;
        this.poolMaxSize = maximumPoolSize;
        this.poolThreadAliveTime = keepAliveTime;
        this.timeUnit = unit;
        this.queueSize = queueSize;
    }

 }
