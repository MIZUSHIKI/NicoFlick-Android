package com.mizushiki.nicoflick_a

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20
import de.psdev.licensesdialog.licenses.MITLicense
import de.psdev.licensesdialog.model.Notice
import de.psdev.licensesdialog.model.Notices
import kotlinx.android.synthetic.main.activity_main.progress_circular
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy


//グローバル
object GLOBAL {
    lateinit var APPLICATIONCONTEXT:Context
    lateinit var COOKIE_MANAGER: CookieManager
    val PHP_URL = "http://timetag.main.jp/nicoflick/nicoflick.php"
    //val PHP_URL = "http://192.168.100.101/nicoflick/nicoflick.php" //windows xampp
    //Activity間 オブジェクト受け渡し用
    var SelectMUSIC:musicData? = null
    var SelectLEVEL:levelData? = null
    var CurrentNOTES:Notes? = null
    var ResultFirst = false
}

class MainActivity : AppCompatActivity() {

    var segueing = false //遷移中フラグ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //アプリケーション最初期実行 処理
        GLOBAL.APPLICATIONCONTEXT = applicationContext //onCreate後に作られる
        val uuid = USERDATA.UserID // ここで Userdata object = シングルトン 初期化 （内部で GLOBAL_APPLICATIONCONTEXTを使用）

        println("log:"+USERDATA.UserID)
        println("log:"+USERDATA.UserIDxxx)

        GLOBAL.COOKIE_MANAGER = CookieManager()
        GLOBAL.COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
        if (CookieHandler.getDefault() != GLOBAL.COOKIE_MANAGER) {
            CookieHandler.setDefault(GLOBAL.COOKIE_MANAGER)
        }
        //
    }

    fun Button_Start(view: View) {
        if(segueing) {
            return
        }
        segueing = true

        progress_circular.isVisible = true
        ServerDataHandler().DownloadMusicDataAndUserNameData {
            //if(it!=null) {
                //return@DownloadMusicDataAndUserNameData
            //}
            progress_circular.isVisible = false

            val intent: Intent = Intent(applicationContext, Activity_Selector::class.java)
            startActivity(intent)
            segueing = false

        }
    }

    fun Button_settings(view: View) {
        val intent: Intent = Intent(applicationContext, Activity_Settings::class.java)
        startActivity(intent)
    }

    fun Button_Info(view: View) {

        val strList = arrayOf("🔗 Google Playで見る","🔗 デザイン協力募集中(GitHub)","ライブラリ クレジット")

        val textView = TextView(applicationContext)
        textView.setTextSize(14.0f)
        textView.setPadding(20)
        textView.setText(
"""NicoFlickはフリック入力リズムゲーである 故「ミクフリック」の パクリ、オマージュ、リスペクト、難民先 作品です。


-2016年7月19日-
ミクフリック/02 サービス終了

-2017年9月29日-
ミクフリック(初代) サービス終了

-2017年9月20日-
iOS11リリース(32bitアプリなので両方起動できなくなる)"""
        )
        AlertDialog.Builder(this) // FragmentではActivityを取得して生成
            .setTitle("NicoFlickについて")
            .setView(textView)
            .setItems(strList, { dialog, which ->
                when(which){
                    0 -> {
                        val url = "https://play.google.com/store/apps/details?id=com.mizushiki.nicoflick_a"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(Intent.createChooser(intent, "Browse with"))
                    }
                    1 -> {
                        val url = "https://github.com/MIZUSHIKI/NicoFlick-Android/blob/master/README.md"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(Intent.createChooser(intent, "Browse with"))
                    }
                    2 -> {
                        ShowLicensesDialog()
                    }
                }
            })
            .setPositiveButton("OK", null)
            .show()

    }

    fun ShowLicensesDialog(){
        val notices = Notices()
        notices.addNotice(
            Notice(
                "CoverFlow",
                "https://github.com/moondroid/CoverFlow",
                "The MIT License (MIT)\n\nCopyright (c) 2014 Marco Granatiero",
                MITLicense()
            ))
        notices.addNotice(
            Notice(
                "OkHttp",
                "https://square.github.io/okhttp/",
                "Copyright 2019 Square, Inc.",
                ApacheSoftwareLicense20()
            ))
        notices.addNotice(
            Notice(
                "ExoPlayer",
                "https://github.com/moondroid/CoverFlow",
                "                                 Apache License\n" +
                        "                           Version 2.0, January 2004\n" +
                        "                        http://www.apache.org/licenses/\n" +
                        "\n" +
                        "   TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION\n" +
                        "\n" +
                        "   1. Definitions.\n" +
                        "\n" +
                        "      \"License\" shall mean the terms and conditions for use, reproduction,\n" +
                        "      and distribution as defined by Sections 1 through 9 of this document.\n" +
                        "\n" +
                        "      \"Licensor\" shall mean the copyright owner or entity authorized by\n" +
                        "      the copyright owner that is granting the License.\n" +
                        "\n" +
                        "      \"Legal Entity\" shall mean the union of the acting entity and all\n" +
                        "      other entities that control, are controlled by, or are under common\n" +
                        "      control with that entity. For the purposes of this definition,\n" +
                        "      \"control\" means (i) the power, direct or indirect, to cause the\n" +
                        "      direction or management of such entity, whether by contract or\n" +
                        "      otherwise, or (ii) ownership of fifty percent (50%) or more of the\n" +
                        "      outstanding shares, or (iii) beneficial ownership of such entity.\n" +
                        "\n" +
                        "      \"You\" (or \"Your\") shall mean an individual or Legal Entity\n" +
                        "      exercising permissions granted by this License.\n" +
                        "\n" +
                        "      \"Source\" form shall mean the preferred form for making modifications,\n" +
                        "      including but not limited to software source code, documentation\n" +
                        "      source, and configuration files.\n" +
                        "\n" +
                        "      \"Object\" form shall mean any form resulting from mechanical\n" +
                        "      transformation or translation of a Source form, including but\n" +
                        "      not limited to compiled object code, generated documentation,\n" +
                        "      and conversions to other media types.\n" +
                        "\n" +
                        "      \"Work\" shall mean the work of authorship, whether in Source or\n" +
                        "      Object form, made available under the License, as indicated by a\n" +
                        "      copyright notice that is included in or attached to the work\n" +
                        "      (an example is provided in the Appendix below).\n" +
                        "\n" +
                        "      \"Derivative Works\" shall mean any work, whether in Source or Object\n" +
                        "      form, that is based on (or derived from) the Work and for which the\n" +
                        "      editorial revisions, annotations, elaborations, or other modifications\n" +
                        "      represent, as a whole, an original work of authorship. For the purposes\n" +
                        "      of this License, Derivative Works shall not include works that remain\n" +
                        "      separable from, or merely link (or bind by name) to the interfaces of,\n" +
                        "      the Work and Derivative Works thereof.\n" +
                        "\n" +
                        "      \"Contribution\" shall mean any work of authorship, including\n" +
                        "      the original version of the Work and any modifications or additions\n" +
                        "      to that Work or Derivative Works thereof, that is intentionally\n" +
                        "      submitted to Licensor for inclusion in the Work by the copyright owner\n" +
                        "      or by an individual or Legal Entity authorized to submit on behalf of\n" +
                        "      the copyright owner. For the purposes of this definition, \"submitted\"\n" +
                        "      means any form of electronic, verbal, or written communication sent\n" +
                        "      to the Licensor or its representatives, including but not limited to\n" +
                        "      communication on electronic mailing lists, source code control systems,\n" +
                        "      and issue tracking systems that are managed by, or on behalf of, the\n" +
                        "      Licensor for the purpose of discussing and improving the Work, but\n" +
                        "      excluding communication that is conspicuously marked or otherwise\n" +
                        "      designated in writing by the copyright owner as \"Not a Contribution.\"\n" +
                        "\n" +
                        "      \"Contributor\" shall mean Licensor and any individual or Legal Entity\n" +
                        "      on behalf of whom a Contribution has been received by Licensor and\n" +
                        "      subsequently incorporated within the Work.\n" +
                        "\n" +
                        "   2. Grant of Copyright License. Subject to the terms and conditions of\n" +
                        "      this License, each Contributor hereby grants to You a perpetual,\n" +
                        "      worldwide, non-exclusive, no-charge, royalty-free, irrevocable\n" +
                        "      copyright license to reproduce, prepare Derivative Works of,\n" +
                        "      publicly display, publicly perform, sublicense, and distribute the\n" +
                        "      Work and such Derivative Works in Source or Object form.\n" +
                        "\n" +
                        "   3. Grant of Patent License. Subject to the terms and conditions of\n" +
                        "      this License, each Contributor hereby grants to You a perpetual,\n" +
                        "      worldwide, non-exclusive, no-charge, royalty-free, irrevocable\n" +
                        "      (except as stated in this section) patent license to make, have made,\n" +
                        "      use, offer to sell, sell, import, and otherwise transfer the Work,\n" +
                        "      where such license applies only to those patent claims licensable\n" +
                        "      by such Contributor that are necessarily infringed by their\n" +
                        "      Contribution(s) alone or by combination of their Contribution(s)\n" +
                        "      with the Work to which such Contribution(s) was submitted. If You\n" +
                        "      institute patent litigation against any entity (including a\n" +
                        "      cross-claim or counterclaim in a lawsuit) alleging that the Work\n" +
                        "      or a Contribution incorporated within the Work constitutes direct\n" +
                        "      or contributory patent infringement, then any patent licenses\n" +
                        "      granted to You under this License for that Work shall terminate\n" +
                        "      as of the date such litigation is filed.\n" +
                        "\n" +
                        "   4. Redistribution. You may reproduce and distribute copies of the\n" +
                        "      Work or Derivative Works thereof in any medium, with or without\n" +
                        "      modifications, and in Source or Object form, provided that You\n" +
                        "      meet the following conditions:\n" +
                        "\n" +
                        "      (a) You must give any other recipients of the Work or\n" +
                        "          Derivative Works a copy of this License; and\n" +
                        "\n" +
                        "      (b) You must cause any modified files to carry prominent notices\n" +
                        "          stating that You changed the files; and\n" +
                        "\n" +
                        "      (c) You must retain, in the Source form of any Derivative Works\n" +
                        "          that You distribute, all copyright, patent, trademark, and\n" +
                        "          attribution notices from the Source form of the Work,\n" +
                        "          excluding those notices that do not pertain to any part of\n" +
                        "          the Derivative Works; and\n" +
                        "\n" +
                        "      (d) If the Work includes a \"NOTICE\" text file as part of its\n" +
                        "          distribution, then any Derivative Works that You distribute must\n" +
                        "          include a readable copy of the attribution notices contained\n" +
                        "          within such NOTICE file, excluding those notices that do not\n" +
                        "          pertain to any part of the Derivative Works, in at least one\n" +
                        "          of the following places: within a NOTICE text file distributed\n" +
                        "          as part of the Derivative Works; within the Source form or\n" +
                        "          documentation, if provided along with the Derivative Works; or,\n" +
                        "          within a display generated by the Derivative Works, if and\n" +
                        "          wherever such third-party notices normally appear. The contents\n" +
                        "          of the NOTICE file are for informational purposes only and\n" +
                        "          do not modify the License. You may add Your own attribution\n" +
                        "          notices within Derivative Works that You distribute, alongside\n" +
                        "          or as an addendum to the NOTICE text from the Work, provided\n" +
                        "          that such additional attribution notices cannot be construed\n" +
                        "          as modifying the License.\n" +
                        "\n" +
                        "      You may add Your own copyright statement to Your modifications and\n" +
                        "      may provide additional or different license terms and conditions\n" +
                        "      for use, reproduction, or distribution of Your modifications, or\n" +
                        "      for any such Derivative Works as a whole, provided Your use,\n" +
                        "      reproduction, and distribution of the Work otherwise complies with\n" +
                        "      the conditions stated in this License.\n" +
                        "\n" +
                        "   5. Submission of Contributions. Unless You explicitly state otherwise,\n" +
                        "      any Contribution intentionally submitted for inclusion in the Work\n" +
                        "      by You to the Licensor shall be under the terms and conditions of\n" +
                        "      this License, without any additional terms or conditions.\n" +
                        "      Notwithstanding the above, nothing herein shall supersede or modify\n" +
                        "      the terms of any separate license agreement you may have executed\n" +
                        "      with Licensor regarding such Contributions.\n" +
                        "\n" +
                        "   6. Trademarks. This License does not grant permission to use the trade\n" +
                        "      names, trademarks, service marks, or product names of the Licensor,\n" +
                        "      except as required for reasonable and customary use in describing the\n" +
                        "      origin of the Work and reproducing the content of the NOTICE file.\n" +
                        "\n" +
                        "   7. Disclaimer of Warranty. Unless required by applicable law or\n" +
                        "      agreed to in writing, Licensor provides the Work (and each\n" +
                        "      Contributor provides its Contributions) on an \"AS IS\" BASIS,\n" +
                        "      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or\n" +
                        "      implied, including, without limitation, any warranties or conditions\n" +
                        "      of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A\n" +
                        "      PARTICULAR PURPOSE. You are solely responsible for determining the\n" +
                        "      appropriateness of using or redistributing the Work and assume any\n" +
                        "      risks associated with Your exercise of permissions under this License.\n" +
                        "\n" +
                        "   8. Limitation of Liability. In no event and under no legal theory,\n" +
                        "      whether in tort (including negligence), contract, or otherwise,\n" +
                        "      unless required by applicable law (such as deliberate and grossly\n" +
                        "      negligent acts) or agreed to in writing, shall any Contributor be\n" +
                        "      liable to You for damages, including any direct, indirect, special,\n" +
                        "      incidental, or consequential damages of any character arising as a\n" +
                        "      result of this License or out of the use or inability to use the\n" +
                        "      Work (including but not limited to damages for loss of goodwill,\n" +
                        "      work stoppage, computer failure or malfunction, or any and all\n" +
                        "      other commercial damages or losses), even if such Contributor\n" +
                        "      has been advised of the possibility of such damages.\n" +
                        "\n" +
                        "   9. Accepting Warranty or Additional Liability. While redistributing\n" +
                        "      the Work or Derivative Works thereof, You may choose to offer,\n" +
                        "      and charge a fee for, acceptance of support, warranty, indemnity,\n" +
                        "      or other liability obligations and/or rights consistent with this\n" +
                        "      License. However, in accepting such obligations, You may act only\n" +
                        "      on Your own behalf and on Your sole responsibility, not on behalf\n" +
                        "      of any other Contributor, and only if You agree to indemnify,\n" +
                        "      defend, and hold each Contributor harmless for any liability\n" +
                        "      incurred by, or claims asserted against, such Contributor by reason\n" +
                        "      of your accepting any such warranty or additional liability.\n" +
                        "\n" +
                        "   END OF TERMS AND CONDITIONS\n" +
                        "\n" +
                        "   APPENDIX: How to apply the Apache License to your work.\n" +
                        "\n" +
                        "      To apply the Apache License to your work, attach the following\n" +
                        "      boilerplate notice, with the fields enclosed by brackets \"[]\"\n" +
                        "      replaced with your own identifying information. (Don't include\n" +
                        "      the brackets!)  The text should be enclosed in the appropriate\n" +
                        "      comment syntax for the file format. We also recommend that a\n" +
                        "      file or class name and description of purpose be included on the\n" +
                        "      same \"printed page\" as the copyright notice for easier\n" +
                        "      identification within third-party archives.\n" +
                        "\n" +
                        "   Copyright [yyyy] [name of copyright owner]",
                ApacheSoftwareLicense20()
            ))
        notices.addNotice(
            Notice(
                "picasso",
                "https://github.com/square/picasso",
                "Copyright 2013 Square, Inc.",
                ApacheSoftwareLicense20()
            ))
        notices.addNotice(
            Notice(
                "minimal-json",
                "https://github.com/ralfstx/minimal-json",
                "Copyright (c) 2013, 2014 EclipseSource",
                MITLicense()
            ))
        notices.addNotice(
            Notice(
                "LicensesDialog",
                "https://github.com/PSDev/LicensesDialog",
                "Copyright 2013 Philip Schiffer",
                ApacheSoftwareLicense20()
            ))
        LicensesDialog.Builder(this).setNotices(notices).build().show()
    }
}
