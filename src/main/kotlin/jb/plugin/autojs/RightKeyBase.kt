package jb.plugin.autojs

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.vfs.VfsUtilCore
import icons.AutoJsIcons
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class RightKeyBase(
    private val device: Device,
) : AnAction("同步项目到 ${device.info.deviceName}[${device.info.ip}]", "同步项目", AutoJsIcons.AutoJs) {

//    override fun update(e: AnActionEvent) {
//        super.update(e)
//        e.presentation.icon = AutoJsIcons.AutoJs
//        e.presentation.text = description
//        e.presentation.description = description
//    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun actionPerformed(e: AnActionEvent) {
        /**
         * 从Action中得到一个虚拟文件
         */
        val virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE)
//        if (virtualFile!!.isDirectory) {
//            virtualFile = virtualFile.parent
//        }
        val parentFile = virtualFile!!.parent
        println(parentFile.path)

        val byteStream = ByteArrayOutputStream()//这个用来最后获取压缩后的字节流
        val outputStream = ZipOutputStream(byteStream)
        //遍历文件夹
        virtualFile.refresh(false, true)//刷新一下缓存
        VfsUtilCore.iterateChildrenRecursively(virtualFile, null) { virtualFile ->
            if (virtualFile.isDirectory) {
                return@iterateChildrenRecursively true
            }
            val byteArr = virtualFile.contentsToByteArray()
            val zipName =
                StringUtils.substringAfter(virtualFile.path, parentFile.path + "/")//压缩文件命名 (例dist\xx\yy\zz.txt)
            outputStream.putNextEntry(ZipEntry(zipName))//思就是我下面io操作(写入)都是在z这个文件条目下进行的。
            outputStream.write(byteArr)
            outputStream.closeEntry() //代表要结束当前条目的写入
            return@iterateChildrenRecursively true
        }
        outputStream.close()
        //压缩好文件,然后就可以发送给设备了
        GlobalScope.launch {
            device.sendProject6Zip(byteStream, parentFile.name, virtualFile.name)
        }

    }
}