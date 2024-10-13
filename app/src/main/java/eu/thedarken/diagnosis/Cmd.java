package eu.thedarken.diagnosis;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;

import android.util.Log;

public class Cmd {
    private final String TAG = "eu.thedarken.diagnosis.Cmd";
    private boolean useRoot = false;
    private int exitcode = 99;
    private Integer timeout = 0;
    private Executor exe;
    private ArrayList<String> commands = new ArrayList<String>();
    private ArrayList<String> output = null;
    private ArrayList<String> errors = null;
    private long SHELLDELAY = 35;
    private boolean DEBUG = false;

    public Cmd() {

    }

    public void execute() {
        exe = new Executor(commands, useRoot);
        if (DEBUG) {
            Log.d(TAG, "셸 딜레이:" + SHELLDELAY);
            if (useRoot)
                Log.d(TAG, "루트로 실행하려고 합니다");
        }
        exe.start();
        try {
            if (timeout == 0) {
                exe.join();
            } else {
                exe.join(timeout);
                if (output == null)
                    output = new ArrayList<String>();
                if (errors == null)
                    errors = new ArrayList<String>();
            }
            if (DEBUG) {
                for (String e : this.getErrors())
                    Log.d(TAG, "오류:" + e);
                for (String s : this.getOutput())
                    Log.d(TAG, "산출:" + s);
            }
        } catch (InterruptedException e) {
            exe.interrupt();
            Thread.currentThread().interrupt();
        }
    }

    public void addCommand(String c) {
        commands.add(c);
    }

    public void clearCommands() {
        this.commands.clear();
    }

    public void useRoot(boolean b) {
        useRoot = b;
    }

    public void setDEBUG(boolean d) {
        DEBUG = d;
    }

    public void setTimeout(int ms) {
        timeout = ms;
    }

    public void setShellDelay(long ms) {
        SHELLDELAY = ms;
    }

    public ArrayList<String> getOutput() {
        return output;
    }

    public ArrayList<String> getErrors() {
        return errors;
    }

    public int getExitCode() {
        return exitcode;
    }

    private class Executor extends Thread {
        Scanner e;
        Scanner s;
        private Process q = null;
        private ArrayList<String> output = new ArrayList<String>();
        private ArrayList<String> errors = new ArrayList<String>();
        private ArrayList<String> commands = new ArrayList<String>();
        private boolean useRoot = false;
        private int exitcode = 0;

        public Executor(ArrayList<String> commands, boolean useRoot) {
            this.commands = commands;
            this.useRoot = useRoot;

        }

        @Override
        public void run() {
            try {
                Thread.sleep(SHELLDELAY);
                q = Runtime.getRuntime().exec(useRoot ? "su" : "sh");
                e = new Scanner(q.getErrorStream());
                s = new Scanner(q.getInputStream());
                OutputStreamWriter os = new OutputStreamWriter(q.getOutputStream());
                // loop commands
                for (String s : commands) {
                    os.write(s + "\n");
                    if (DEBUG)
                        Log.d(TAG, s);
                }

                os.write("exit\n");
                os.flush();
                os.close();

                while (e.hasNextLine())
                    errors.add(e.nextLine());
                e.close();
                while (s.hasNextLine())
                    output.add(s.nextLine());
                s.close();

                exitcode = q.waitFor();
                if (DEBUG) Log.d(TAG, "종료코드: " + exitcode);
            } catch (InterruptedException interrupt) {
                if (DEBUG)
                    Log.i(TAG, "중단된!");
                exitcode = 130;
                return;
            } catch (IOException e) {
                if (DEBUG)
                    Log.i(TAG, "IOException, 명령이 실패했습니다. 찾을 수 없습니까?");
                exitcode = 127;
            } finally {
                if (q != null)
                    q.destroy();
                if (e != null)
                    e.close();
                if (s != null)
                    s.close();

                Cmd.this.output = this.output;
                Cmd.this.errors = this.errors;
                Cmd.this.exitcode = this.exitcode;
            }
        }
    }


}
