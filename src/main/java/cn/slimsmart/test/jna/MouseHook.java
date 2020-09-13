
package cn.slimsmart.test.jna;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.MSG;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * 鼠标钩子
 */
public class MouseHook {
    //鼠标事件编码
    public User32 lib;
    private static HHOOK hhk;
    private MouseHookListener mouseHook;
    private HMODULE hMod;
    private boolean isWindows = false;

    public MouseHook() {
        isWindows = Platform.isWindows();
        if (isWindows) {
            lib = User32.INSTANCE;
            hMod = Kernel32.INSTANCE.GetModuleHandle(null);
        }

    }

    //添加钩子监听
    public void addMouseHookListener(MouseHookListener mouseHook) {
        this.mouseHook = mouseHook;
        this.mouseHook.lib = lib;
    }

    //启动
    public void startWindowsHookEx() {
        if (isWindows) {
            lib.SetWindowsHookEx(WinUser.WH_MOUSE_LL, mouseHook, hMod, 0);
            int result;
            MSG msg = new MSG();
            while ((result = lib.GetMessage(msg, null, 0, 0)) != 0) {
                if (result == -1) {
                    System.err.println("error in get message");
                    break;
                } else {
                    System.err.println("got message");
                    lib.TranslateMessage(msg);
                    lib.DispatchMessage(msg);
                }
            }
        }

    }

    //关闭
    public void stopWindowsHookEx() {
        if (isWindows) {
            lib.UnhookWindowsHookEx(hhk);
        }

    }

    public static void main(String[] args) {
        try {
            MouseHook mouseHook = new MouseHook();
            mouseHook.addMouseHookListener(new MouseHookListener() {
                //回调监听
                public LRESULT callback(int nCode, WPARAM wParam, MouseHookStruct lParam) {
                    Robot robot = null;
                    try {
                        robot = new Robot();
                    } catch (AWTException e) {
                        System.out.println(e.getStackTrace());
                    }
                    if (nCode >= 0) {
                        if (wParam.intValue() == 523) {
                            return new LRESULT(1);
                        }
                        if (wParam.intValue() == 524) {
                            if (lParam.hwnd.getPointer().toString().equals("native@0x10000")) {
                                System.out.println(lParam.hwnd.getPointer().toString());
                                System.out.println(lParam.hwnd.getPointer().toString().equals("native@0x10000"));
                                //按下crtl v键 ；
                                robot.keyPress(KeyEvent.VK_CONTROL);
                                robot.keyPress(KeyEvent.VK_V);
                                //释放crtl v 键
                                robot.keyRelease(KeyEvent.VK_V);
                                robot.keyRelease(KeyEvent.VK_CONTROL);
                                return new LRESULT(1);
                            }
                            if (lParam.hwnd.getPointer().toString().equals("native@0x20000")) {
                                //按下crtl v键 ；
                                robot.keyPress(KeyEvent.VK_CONTROL);
                                robot.keyPress(KeyEvent.VK_C);
                                //释放crtl v 键
                                robot.keyRelease(KeyEvent.VK_C);
                                robot.keyRelease(KeyEvent.VK_CONTROL);
                            }
                        }
                    }


                    //将钩子信息传递到当前钩子链中的下一个子程，一个钩子程序可以调用这个函数之前或之后处理钩子信息
                    //hhk：当前钩子的句柄
                    //nCode ：钩子代码; 就是给下一个钩子要交待的，钩传递给当前Hook过程的代码。下一个钩子程序使用此代码，以确定如何处理钩的信息。
                    //wParam：要传递的参数; 由钩子类型决定是什么参数，此参数的含义取决于当前的钩链与钩的类型。
                    //lParam：Param的值传递给当前Hook过程。此参数的含义取决于当前的钩链与钩的类型。
                    return lib.CallNextHookEx(hhk, nCode, wParam, lParam.getPointer());
                }
            });
            mouseHook.startWindowsHookEx();
            Thread.sleep(20000);
            mouseHook.stopWindowsHookEx();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
