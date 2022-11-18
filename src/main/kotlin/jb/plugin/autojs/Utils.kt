package jb.plugin.autojs

import java.io.File
import java.io.IOException
import java.net.*
import java.security.MessageDigest
import java.util.*

class Utils {
    companion object {
        //使用当前时间和随机数生成一个唯一MessageId
        fun genMessageId(): String {
            return "${System.currentTimeMillis()}_${Math.random()}"
        }

        /**
         * 获取内网IP地址
         */
        val localIPAddress: String
            get() {
                val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intF: NetworkInterface = en.nextElement()
                    val enumIpAddr: Enumeration<InetAddress> = intF.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress: InetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                            return inetAddress.hostAddress.toString()
                        }
                    }
                }
                return "null"
            }

        /**
         * 查看本机某端口是否被占用
         *
         * @param port 端口号
         * @return 如果被占用则返回true，否则返回false
         */
        fun isLocalPortUsing(port: Int): Boolean {
            var flag = true
            try {
                flag = isPortUsing("127.0.0.1", port)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return flag
        }

        /**
         * 根据IP和端口号，查询其是否被占用
         * @param host  IP
         * @param port  端口号
         * @return  如果被占用，返回true；否则返回false
         * @throws UnknownHostException    IP地址不通或错误，则会抛出此异常
         */
        @Throws(UnknownHostException::class)
        fun isPortUsing(host: String?, port: Int): Boolean {
            var flag = false
            val theAddress = InetAddress.getByName(host)
            try {
                val socket = Socket(theAddress, port)
                flag = true
            } catch (e: IOException) {
                //如果所测试端口号没有被占用，那么会抛出异常，这里利用这个机制来判断
                //所以，这里在捕获异常后，什么也不用做
            }
            return flag
        }

        fun toHex(byteArray: ByteArray): String {
            val result = with(StringBuilder()) {
                byteArray.forEach {
                    val hex = it.toInt() and (0xFF)
                    val hexStr = Integer.toHexString(hex)
                    if (hexStr.length == 1) {
                        this.append("0").append(hexStr)
                    } else {
                        this.append(hexStr)
                    }
                }
                this.toString()
            }
            //转成16进制后是32 字节
            return result
        }

        //计算md5
        fun computeMd5(bs: ByteArray): String {
            val digest = MessageDigest.getInstance("MD5")//用来计算MD5
            digest.update(bs)
            return toHex(digest.digest())
        }

        fun createFile(filePath: String): File {
            val file = File(filePath)
            val parentFile = file.parentFile!!
            if (!parentFile.exists()) {
                parentFile.mkdirs()
            }
            if (!file.exists()) {
                file.createNewFile()
            }
            return file
        }
    }
}