﻿using System;
using System.Runtime.InteropServices;

/**
 * In order to get this sample running in Visual Studio 2010:
 *
 * 1. Add the umundo .net module:
 *    Right-click "References" in the "Solution Explorer" and select "Add Reference"
 *    Navigate to your umundo build folder and in lib/ choose umundoCSharp.dll
 *
 * 2. Make sure the actual umundoNativeCSharp.dll is found via DllImport
 *    There are a couple of possibilities to make the dll known to the runtime:
 *    http://msdn.microsoft.com/en-us/library/ms682586.aspx
 *
 *    Either add ${umundo_build_folder}/lib in the %PATH% environment variable or add
 *    the full path via SetDllDirectory() [as is shown below].
 *
 * 3. Run the application with other umundo-pingpong instances and see ioioio...
 */

namespace umundo_pingpong
{
    using org.umundo.core;

    class PingReceiver : Receiver {

        public override void receive(Message msg) {
            Console.Write("i");
        }
    }

    class Program
    {
        [DllImport("kernel32.dll", CharSet = CharSet.Auto)]
        private static extern void SetDllDirectory(string lpPathName);

        static void Main(string[] args)
        {
            /*
             * Make sure this path contains the umundoNativeCSharp.dll!
             */
          if (System.Environment.Is64BitProcess)
          {
             SetDllDirectory("C:\\Program Files (x86)\\uMundo\\share\\umundo\\bindings\\csharp64");
          }
          else
          {
           SetDllDirectory("C:\\Program Files (x86)\\uMundo\\share\\umundo\\bindings\\csharp");
          }

            org.umundo.core.Node node = new org.umundo.core.Node();
            org.umundo.core.Discovery disc = new org.umundo.core.Discovery(Discovery.DiscoveryType.MDNS);
            disc.add(node);

            Publisher pub = new Publisher("pingpong");
            PingReceiver recv = new PingReceiver();
            Subscriber sub = new Subscriber("pingpong", recv);
            node.addPublisher(pub);
            node.addSubscriber(sub);

            while (!Console.KeyAvailable)
            {
                Message msg = new Message();
                String data = "data";
                System.Text.ASCIIEncoding enc = new System.Text.ASCIIEncoding();
                byte[] buffer = enc.GetBytes(data);
                msg.setData(buffer);
                msg.putMeta("foo", "bar");
                Console.Write("o");
                pub.send(msg);
                System.Threading.Thread.Sleep(1000);
            }
        }
    }
}
