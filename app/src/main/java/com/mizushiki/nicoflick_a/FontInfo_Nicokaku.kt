package com.mizushiki.nicoflick_a


object FontInfo_NicoKaku1 {
    //表示できるUnicodeの範囲 ; フォント内で空白文字(U+0020,U+3000以外で)になってそうなのは弾いている
    val wcRanges : Array<Int> = arrayOf(0x0020,95, 0x00A7,2, 0x00B0,2, 0x00B4,1, 0x00B6,1, 0x00D7,1, 0x00F7,1, 0x0391,17, 0x03A3,7, 0x03B1,17, 0x03C3,7, 0x0401,1, 0x0410,64, 0x0451,1, 0x2010,1, 0x2015,1, 0x2018,2, 0x201C,2, 0x2020,2, 0x2026,1, 0x2030,1, 0x2032,2, 0x203B,1, 0x2103,1, 0x2116,1, 0x212B,1, 0x2160,10, 0x2170,10, 0x2190,4, 0x21D2,1, 0x21D4,1, 0x2200,1, 0x2202,2, 0x2207,2, 0x220B,1, 0x2211,1, 0x221A,1, 0x221D,4, 0x2225,1, 0x2227,6, 0x222E,1, 0x2234,2, 0x223D,1, 0x2252,1, 0x2260,2, 0x2266,2, 0x226A,2, 0x2282,2, 0x2286,2, 0x22A5,1, 0x22BF,1, 0x2312,1, 0x2460,20, 0x2500,4, 0x250C,1, 0x250F,2, 0x2513,2, 0x2517,2, 0x251B,3, 0x2520,1, 0x2523,3, 0x2528,1, 0x252B,2, 0x252F,2, 0x2533,2, 0x2537,2, 0x253B,2, 0x253F,1, 0x2542,1, 0x254B,1, 0x25A0,2, 0x25B2,2, 0x25BC,2, 0x25C6,2, 0x25CB,1, 0x25CE,2, 0x25EF,1, 0x2605,2, 0x2640,1, 0x2642,1, 0x266A,1, 0x266D,1, 0x266F,1, 0x3000,4, 0x3005,17, 0x301D,1, 0x301F,1, 0x3041,83, 0x309B,4, 0x30A1,86, 0x30FB,4, 0x4E00,2, 0x4E03,1, 0x4E07,5, 0x4E0D,2, 0x4E11,1, 0x4E14,3, 0x4E18,2, 0x4E1E,1, 0x4E21,1, 0x4E26,1, 0x4E28,1, 0x4E2A,1, 0x4E2D,1, 0x4E32,1, 0x4E36,1, 0x4E38,2, 0x4E3B,2, 0x4E3F,1, 0x4E43,1, 0x4E45,1, 0x4E4B,1, 0x4E4D,3, 0x4E56,4, 0x4E5D,3, 0x4E62,1, 0x4E71,1, 0x4E73,1, 0x4E7E,1, 0x4E80,1, 0x4E85,2, 0x4E88,2, 0x4E8B,2, 0x4E8E,1, 0x4E91,2, 0x4E94,2, 0x4E98,2, 0x4E9B,2, 0x4E9E,5, 0x4EA4,3, 0x4EA8,1, 0x4EAB,4, 0x4EB6,1, 0x4EBA,1, 0x4EC0,3, 0x4EC4,1, 0x4EC6,2, 0x4ECA,2, 0x4ECE,2, 0x4ED4,6, 0x4EDD,1, 0x4EDF,1, 0x4EE3,3, 0x4EED,2, 0x4EF0,1, 0x4EF2,1, 0x4EF6,1, 0x4EFB,1, 0x4F01,1, 0x4F09,2, 0x4F0D,5, 0x4F1A,1, 0x4F1C,2, 0x4F2F,2, 0x4F34,1, 0x4F36,1, 0x4F38,1, 0x4F3A,1, 0x4F3C,2, 0x4F43,1, 0x4F46,2, 0x4F4D,5, 0x4F53,1, 0x4F55,1, 0x4F57,1, 0x4F59,5, 0x4F69,1, 0x4F6F,2, 0x4F73,1, 0x4F75,2, 0x4F7B,2, 0x4F7F,1, 0x4F83,1, 0x4F86,1, 0x4F88,1, 0x4F8B,1, 0x4F8D,1, 0x4F8F,1, 0x4F91,1, 0x4F96,1, 0x4F98,1, 0x4F9B,1, 0x4F9D,1, 0x4FA0,2, 0x4FAD,3, 0x4FB5,2, 0x4FBF,1, 0x4FC2,3, 0x4FCA,1, 0x4FCE,1, 0x4FD1,1, 0x4FD4,1, 0x4FD7,2, 0x4FDA,2, 0x4FDD,1, 0x4FDF,1, 0x4FE1,1, 0x4FE3,3, 0x4FEE,2, 0x4FF3,1, 0x4FF5,2, 0x4FF8,1, 0x4FFA,1, 0x4FFE,1, 0x5005,2, 0x5009,1, 0x500B,1, 0x500D,1, 0x5012,1, 0x5014,1, 0x5016,1, 0x5019,2, 0x501F,1, 0x5021,1, 0x5023,4, 0x5028,1, 0x502A,4, 0x5036,1, 0x5039,1, 0x5043,1, 0x5047,3, 0x504F,1, 0x5055,1, 0x505A,1, 0x505C,1, 0x5065,1, 0x5072,1, 0x5074,3, 0x507D,1, 0x5080,1, 0x5085,1, 0x508D,1, 0x5091,1, 0x5098,2, 0x50AC,2, 0x50B2,4, 0x50B7,1, 0x50BE,1, 0x50C2,1, 0x50C5,1, 0x50C9,1, 0x50CD,1, 0x50CF,1, 0x50D1,1, 0x50D5,2, 0x50DA,1, 0x50E3,1, 0x50E5,1, 0x50E7,1, 0x50ED,2, 0x50F5,1, 0x50FB,1, 0x5100,1, 0x5102,1, 0x5104,1, 0x5109,1, 0x5112,1, 0x5114,1, 0x5116,1, 0x5118,1, 0x511A,1, 0x511F,1, 0x5121,1, 0x512A,1, 0x5132,1, 0x513A,1, 0x513F,3, 0x5143,7, 0x514B,4, 0x5150,1, 0x5152,1, 0x5154,1, 0x515A,1, 0x515C,1, 0x5165,1, 0x5168,6, 0x5171,1, 0x5175,4, 0x517C,1, 0x5182,1, 0x5185,2, 0x518A,1, 0x518D,1, 0x518F,1, 0x5191,3, 0x5196,2, 0x5199,1, 0x51A0,1, 0x51A4,3, 0x51A8,1, 0x51AA,3, 0x51B2,6, 0x51C4,3, 0x51C9,1, 0x51CB,3, 0x51D6,1, 0x51DB,3, 0x51E0,2, 0x51E6,2, 0x51EA,1, 0x51F0,2, 0x51F5,2, 0x51F8,3, 0x51FD,1, 0x5200,1, 0x5203,2, 0x5206,3, 0x520A,1, 0x520E,1, 0x5211,1, 0x5217,1, 0x521D,1, 0x5224,2, 0x5229,2, 0x522E,1, 0x5230,1, 0x5236,6, 0x5243,2, 0x5247,1, 0x524A,4, 0x5256,1, 0x525B,1, 0x5263,3, 0x5269,2, 0x526F,4, 0x5275,1, 0x527D,1, 0x5283,1, 0x5287,3, 0x5294,1, 0x529B,1, 0x529F,2, 0x52A3,1, 0x52A9,3, 0x52B1,1, 0x52B4,1, 0x52B9,1, 0x52BE,1, 0x52C1,1, 0x52C3,1, 0x52C5,1, 0x52C7,1, 0x52C9,1, 0x52CD,1, 0x52D2,1, 0x52D5,1, 0x52D8,2, 0x52DD,3, 0x52E2,1, 0x52E4,1, 0x52E7,1, 0x52F2,2, 0x52F9,2, 0x52FE,2, 0x5301,2, 0x5305,2, 0x5308,1, 0x530D,1, 0x5310,1, 0x5315,3, 0x5319,2, 0x531D,1, 0x5320,2, 0x532A,1, 0x5338,4, 0x533F,3, 0x5343,1, 0x5346,3, 0x534A,1, 0x534D,1, 0x5351,4, 0x5357,2, 0x535A,1, 0x535C,1, 0x5360,1, 0x5366,1, 0x5369,1, 0x536F,3, 0x5373,3, 0x5377,2, 0x537F,1, 0x5382,1, 0x5384,1, 0x5398,1, 0x539A,1, 0x539F,2, 0x53A5,2, 0x53A8,2, 0x53AD,1, 0x53B3,1, 0x53B6,1, 0x53BB,1, 0x53C2,2, 0x53C8,7, 0x53D4,1, 0x53D6,2, 0x53D9,1, 0x53DB,1, 0x53DF,1, 0x53E1,5, 0x53E9,11, 0x53F6,3, 0x53FA,1, 0x5403,2, 0x5408,10, 0x541B,1, 0x541D,1, 0x541F,2, 0x5426,1, 0x5429,1, 0x542B,3, 0x5438,2, 0x543B,4, 0x5442,1, 0x5446,1, 0x5448,3, 0x544E,1, 0x5451,1, 0x545F,1, 0x5468,1, 0x546A,1, 0x5471,1, 0x5473,1, 0x5475,1, 0x5477,1, 0x547B,3, 0x5480,1, 0x5484,1, 0x5486,1, 0x548B,2, 0x548E,1, 0x5490,1, 0x5492,1, 0x54A2,1, 0x54A4,2, 0x54A8,1, 0x54AB,2, 0x54AF,1, 0x54B2,2, 0x54B8,1, 0x54BC,3, 0x54C0,3, 0x54C4,1, 0x54C7,3, 0x54D8,1, 0x54E1,1, 0x54E6,1, 0x54E8,2, 0x54ED,2, 0x54F2,1, 0x54FA,1, 0x54FD,1, 0x5504,1, 0x5506,2, 0x550F,2, 0x5514,1, 0x5516,1, 0x552F,1, 0x5531,1, 0x5533,1, 0x5538,1, 0x553E,1, 0x5540,1, 0x5544,3, 0x554C,1, 0x554F,1, 0x5553,1, 0x5556,2, 0x555C,1, 0x557C,1, 0x557E,1, 0x5580,1, 0x5583,2, 0x5587,1, 0x5589,3, 0x5598,3, 0x559C,4, 0x55A7,1, 0x55A9,4, 0x55AE,1, 0x55B0,1, 0x55B6,1, 0x55C4,2, 0x55C7,1, 0x55D4,1, 0x55DA,1, 0x55DC,1, 0x55DF,1, 0x55E3,2, 0x55F9,1, 0x5606,1, 0x5609,1, 0x5614,1, 0x5616,3, 0x5629,1, 0x562F,1, 0x5631,2, 0x5634,1, 0x5638,1, 0x5642,1, 0x564C,1, 0x565B,1, 0x5664,1, 0x5668,1, 0x566A,2, 0x5674,1, 0x5678,1, 0x567A,1, 0x5686,2, 0x56A0,1, 0x56A2,1, 0x56A5,1, 0x56B4,1, 0x56B6,1, 0x56BC,1, 0x56C0,2, 0x56C3,1, 0x56D7,1, 0x56DA,2, 0x56DE,1, 0x56E0,1, 0x56E3,1, 0x56EE,1, 0x56F0,1, 0x56F2,2, 0x56FA,1, 0x56FD,1, 0x5700,1, 0x5703,1, 0x5708,1, 0x570B,1, 0x570F,1, 0x5712,2, 0x5718,1, 0x571F,1, 0x5726,3, 0x572D,1, 0x5730,1, 0x5738,1, 0x573B,1, 0x5740,1, 0x5742,1, 0x5747,1, 0x574A,1, 0x574E,4, 0x5761,1, 0x5764,1, 0x5766,1, 0x5769,2, 0x577F,1, 0x5782,1, 0x5789,1, 0x578B,1, 0x5793,1, 0x57A0,1, 0x57A2,3, 0x57AA,1, 0x57B0,1, 0x57B3,1, 0x57C3,1, 0x57CB,1, 0x57CE,1, 0x57D2,1, 0x57D4,1, 0x57DC,1, 0x57DF,2, 0x57E3,1, 0x57F4,1, 0x57F7,1, 0x57F9,2, 0x57FC,1, 0x5800,1, 0x5802,1, 0x5805,2, 0x580A,2, 0x5815,1, 0x5819,1, 0x581D,1, 0x5821,1, 0x5824,1, 0x582A,1, 0x582F,3, 0x5834,2, 0x583A,1, 0x5840,2, 0x584A,1, 0x5851,1, 0x5854,1, 0x5857,4, 0x585E,1, 0x5869,1, 0x586B,1, 0x5875,1, 0x5879,1, 0x587E,1, 0x5883,1, 0x5893,1, 0x5897,1, 0x589C,1, 0x589F,1, 0x58A8,1, 0x58B3,1, 0x58B8,1, 0x58BA,1, 0x58BE,1, 0x58C1,1, 0x58C7,1, 0x58CA,1, 0x58CC,1, 0x58D3,1, 0x58D5,1, 0x58D8,1, 0x58DC,1, 0x58DE,1, 0x58E4,1, 0x58EB,2, 0x58EE,5, 0x58F7,1, 0x58F9,3, 0x58FD,1, 0x5902,1, 0x5909,2, 0x590F,1, 0x5915,2, 0x5918,3, 0x591C,1, 0x5922,1, 0x5925,1, 0x5927,1, 0x5929,6, 0x5931,1, 0x5937,2, 0x593E,1, 0x5944,1, 0x5947,3, 0x594E,4, 0x5954,1, 0x5957,2, 0x5960,1, 0x5962,1, 0x5965,1, 0x5967,2, 0x596A,1, 0x596E,1, 0x5973,2, 0x5978,1, 0x597D,1, 0x5981,4, 0x598A,1, 0x5993,1, 0x5996,1, 0x5999,1, 0x59A3,1, 0x59A5,1, 0x59A8,1, 0x59AC,1, 0x59B2,1, 0x59B9,1, 0x59BB,1, 0x59BE,1, 0x59C6,1, 0x59C9,1, 0x59CB,1, 0x59D0,2, 0x59D3,2, 0x59DA,1, 0x59DC,1, 0x59E5,2, 0x59E8,1, 0x59EA,2, 0x59F6,1, 0x59FB,1, 0x59FF,1, 0x5A01,1, 0x5A03,1, 0x5A09,1, 0x5A11,1, 0x5A18,1, 0x5A1A,1, 0x5A1C,1, 0x5A1F,2, 0x5A25,1, 0x5A29,1, 0x5A2F,1, 0x5A35,2, 0x5A3C,1, 0x5A41,1, 0x5A46,1, 0x5A49,1, 0x5A5A,1, 0x5A62,1, 0x5A66,1, 0x5A6A,1, 0x5A7F,1, 0x5A92,1, 0x5A9A,2, 0x5ABD,1, 0x5AC1,2, 0x5AC9,1, 0x5ACC,1, 0x5AD0,1, 0x5AD6,2, 0x5AE1,1, 0x5AE6,1, 0x5B09,1, 0x5B0B,2, 0x5B22,1, 0x5B2A,1, 0x5B2C,1, 0x5B30,1, 0x5B32,1, 0x5B40,1, 0x5B43,1, 0x5B45,1, 0x5B50,1, 0x5B54,2, 0x5B57,2, 0x5B5A,1, 0x5B5C,2, 0x5B5F,1, 0x5B63,2, 0x5B66,1, 0x5B69,1, 0x5B6B,1, 0x5B70,1, 0x5B75,1, 0x5B78,1, 0x5B7A,1, 0x5B80,1, 0x5B83,1, 0x5B85,1, 0x5B87,3, 0x5B8B,3, 0x5B8F,1, 0x5B95,1, 0x5B97,7, 0x5B9F,1, 0x5BA2,4, 0x5BAE,1, 0x5BB0,1, 0x5BB3,4, 0x5BB8,2, 0x5BBF,1, 0x5BC2,1, 0x5BC4,4, 0x5BCC,1, 0x5BD2,2, 0x5BDB,1, 0x5BDD,1, 0x5BDF,1, 0x5BE1,1, 0x5BE6,2, 0x5BE9,1, 0x5BEE,1, 0x5BF5,1, 0x5BF8,1, 0x5BFA,1, 0x5BFE,2, 0x5C01,2, 0x5C04,8, 0x5C0D,3, 0x5C11,1, 0x5C13,1, 0x5C16,1, 0x5C1A,1, 0x5C20,1, 0x5C22,1, 0x5C24,1, 0x5C2D,1, 0x5C31,1, 0x5C38,10, 0x5C45,1, 0x5C48,1, 0x5C4A,2, 0x5C4D,3, 0x5C51,1, 0x5C53,1, 0x5C55,1, 0x5C5E,1, 0x5C60,2, 0x5C64,2, 0x5C6E,2, 0x5C71,1, 0x5C79,1, 0x5C90,2, 0x5CA1,1, 0x5CA8,2, 0x5CAB,2, 0x5CB1,1, 0x5CB3,1, 0x5CB6,3, 0x5CBA,2, 0x5CBE,1, 0x5CC5,1, 0x5CD9,1, 0x5CE0,2, 0x5CE8,1, 0x5CEA,1, 0x5CEF,2, 0x5CF6,1, 0x5CFA,2, 0x5CFD,1, 0x5D07,1, 0x5D0B,1, 0x5D0E,1, 0x5D11,1, 0x5D14,7, 0x5D22,1, 0x5D29,1, 0x5D4B,2, 0x5D4E,1, 0x5D50,1, 0x5D52,1, 0x5D69,1, 0x5D6F,1, 0x5D82,1, 0x5D87,1, 0x5D8B,1, 0x5D9D,1, 0x5DA2,1, 0x5DAC,1, 0x5DAE,1, 0x5DBA,1, 0x5DBC,2, 0x5DCC,2, 0x5DD2,1, 0x5DDB,1, 0x5DDD,2, 0x5DE1,1, 0x5DE3,1, 0x5DE5,4, 0x5DEB,1, 0x5DEE,1, 0x5DF1,4, 0x5DF7,1, 0x5DFB,1, 0x5DFD,2, 0x5E02,2, 0x5E06,1, 0x5E0C,1, 0x5E16,1, 0x5E19,3, 0x5E1D,1, 0x5E25,1, 0x5E2B,1, 0x5E2D,1, 0x5E2F,2, 0x5E33,1, 0x5E36,3, 0x5E3D,1, 0x5E40,1, 0x5E45,1, 0x5E47,1, 0x5E4C,1, 0x5E4E,1, 0x5E54,2, 0x5E57,1, 0x5E5F,1, 0x5E61,3, 0x5E72,5, 0x5E78,8, 0x5E81,1, 0x5E83,2, 0x5E87,1, 0x5E8A,1, 0x5E8F,1, 0x5E95,3, 0x5E9A,1, 0x5E9C,1, 0x5EA6,2, 0x5EAB,1, 0x5EAD,1, 0x5EB5,4, 0x5EC1,1, 0x5EC3,1, 0x5EC9,2, 0x5ED3,1, 0x5EDB,1, 0x5EDF,2, 0x5EE2,2, 0x5EF3,2, 0x5EF6,3, 0x5EFA,3, 0x5EFE,2, 0x5F01,1, 0x5F04,1, 0x5F09,5, 0x5F0F,3, 0x5F13,3, 0x5F17,2, 0x5F1B,1, 0x5F1F,1, 0x5F25,3, 0x5F29,1, 0x5F2F,1, 0x5F31,1, 0x5F35,1, 0x5F37,2, 0x5F3C,1, 0x5F3E,1, 0x5F48,1, 0x5F4A,1, 0x5F4C,1, 0x5F4E,1, 0x5F51,1, 0x5F53,1, 0x5F56,2, 0x5F59,1, 0x5F61,2, 0x5F66,1, 0x5F69,5, 0x5F70,2, 0x5F73,1, 0x5F77,1, 0x5F79,1, 0x5F7C,1, 0x5F7F,7, 0x5F87,2, 0x5F8A,3, 0x5F90,4, 0x5F97,2, 0x5F9E,1, 0x5FA0,2, 0x5FA8,3, 0x5FAD,2, 0x5FB3,2, 0x5FB9,1, 0x5FBD,1, 0x5FC3,1, 0x5FC5,1, 0x5FCC,2, 0x5FD6,4, 0x5FDC,2, 0x5FE0,1, 0x5FE4,1, 0x5FEB,1, 0x5FF0,2, 0x5FF5,1, 0x5FF8,1, 0x5FFD,1, 0x6010,1, 0x6012,1, 0x6015,2, 0x6019,1, 0x601B,3, 0x6020,2, 0x6025,1, 0x6027,5, 0x602F,1, 0x6031,1, 0x6042,2, 0x6046,1, 0x604A,2, 0x604D,1, 0x6050,1, 0x6052,1, 0x6055,1, 0x6059,1, 0x6062,2, 0x6065,1, 0x6068,6, 0x606F,2, 0x6075,1, 0x6081,1, 0x6089,1, 0x608C,2, 0x6094,1, 0x6097,1, 0x609A,2, 0x609F,2, 0x60A3,1, 0x60A6,1, 0x60A9,2, 0x60B2,5, 0x60B8,1, 0x60BC,2, 0x60C5,3, 0x60D1,1, 0x60D8,1, 0x60DA,1, 0x60DC,1, 0x60DF,3, 0x60E3,1, 0x60E7,2, 0x60F0,2, 0x60F3,2, 0x60F6,1, 0x60F9,2, 0x6100,2, 0x6103,1, 0x6108,2, 0x610E,2, 0x6115,1, 0x611A,2, 0x611F,1, 0x6127,1, 0x6134,1, 0x613C,2, 0x6144,1, 0x6147,2, 0x614B,2, 0x614E,1, 0x6153,1, 0x6155,1, 0x6159,2, 0x6162,2, 0x6167,2, 0x616E,1, 0x6170,2, 0x6173,1, 0x6175,3, 0x617E,1, 0x6182,1, 0x618E,1, 0x6190,2, 0x6194,1, 0x619A,1, 0x61A4,1, 0x61A7,1, 0x61A9,1, 0x61AB,2, 0x61AE,1, 0x61B2,1, 0x61B6,1, 0x61BA,1, 0x61BE,1, 0x61C6,2, 0x61C9,2, 0x61D0,1, 0x61E6,1, 0x61F2,1, 0x61F4,1, 0x61F7,2, 0x61FA,1, 0x61FD,4, 0x6208,3, 0x620C,3, 0x6210,3, 0x6214,1, 0x6216,1, 0x621A,1, 0x621F,1, 0x6221,1, 0x6226,1, 0x622A,1, 0x622E,3, 0x6234,1, 0x6238,1, 0x623B,1, 0x623F,3, 0x6247,3, 0x624B,1, 0x624D,2, 0x6253,1, 0x6255,1, 0x6258,1, 0x625B,1, 0x625E,1, 0x6263,1, 0x6268,1, 0x626E,1, 0x6271,1, 0x6276,1, 0x6279,1, 0x627F,2, 0x6282,1, 0x6284,1, 0x6289,2, 0x6291,2, 0x6294,2, 0x6297,2, 0x629C,1, 0x629E,1, 0x62AB,2, 0x62B1,1, 0x62B5,1, 0x62B9,1, 0x62BB,3, 0x62C2,1, 0x62C5,6, 0x62CC,2, 0x62CF,5, 0x62D7,3, 0x62DB,1, 0x62DD,1, 0x62E0,2, 0x62EC,3, 0x62F1,1, 0x62F3,1, 0x62F5,3, 0x62FE,2, 0x6301,2, 0x6307,1, 0x6309,1, 0x630C,1, 0x6311,1, 0x6319,1, 0x631F,1, 0x6328,1, 0x632B,1, 0x632F,1, 0x633A,1, 0x633D,3, 0x6349,1, 0x634C,2, 0x634F,2, 0x6355,1, 0x6357,1, 0x635C,1, 0x6367,3, 0x636E,1, 0x6372,1, 0x6376,2, 0x637A,2, 0x6383,1, 0x6388,2, 0x638C,1, 0x638E,2, 0x6392,1, 0x6396,1, 0x6398,1, 0x639B,1, 0x639F,4, 0x63A5,1, 0x63A7,6, 0x63B2,1, 0x63B4,1, 0x63BB,1, 0x63BE,1, 0x63C0,1, 0x63C3,2, 0x63C6,1, 0x63C9,1, 0x63CF,2, 0x63D6,1, 0x63DA,2, 0x63E1,1, 0x63E3,1, 0x63E9,1, 0x63EE,1, 0x63F4,1, 0x63F6,1, 0x63FA,1, 0x640D,1, 0x640F,1, 0x6413,1, 0x6416,2, 0x641C,1, 0x642C,2, 0x6436,1, 0x643A,1, 0x643E,1, 0x6442,1, 0x644E,1, 0x6458,1, 0x6467,1, 0x6469,1, 0x646F,1, 0x6476,1, 0x6478,1, 0x647A,1, 0x6483,1, 0x6488,1, 0x6492,2, 0x649A,1, 0x649E,1, 0x64A4,2, 0x64A9,1, 0x64AB,1, 0x64AD,2, 0x64B0,1, 0x64B2,1, 0x64B9,1, 0x64BC,1, 0x64C1,1, 0x64C5,1, 0x64C7,1, 0x64CD,1, 0x64D2,1, 0x64D4,1, 0x64E2,2, 0x64E6,2, 0x64EC,1, 0x64EF,1, 0x64F2,1, 0x64F6,1, 0x64FD,2, 0x6500,1, 0x6505,1, 0x6518,1, 0x651D,1, 0x6523,2, 0x652B,1, 0x652F,1, 0x6534,3, 0x6538,2, 0x653B,1, 0x653E,2, 0x6545,1, 0x6548,1, 0x654F,1, 0x6551,1, 0x6555,5, 0x6562,2, 0x6566,1, 0x656C,1, 0x6570,1, 0x6572,1, 0x6574,2, 0x6577,2, 0x6582,2, 0x6587,1, 0x6589,1, 0x658C,1, 0x658E,1, 0x6590,2, 0x6597,1, 0x6599,1, 0x659C,1, 0x659F,1, 0x65A1,1, 0x65A4,2, 0x65A7,1, 0x65AC,2, 0x65AF,2, 0x65B9,1, 0x65BC,2, 0x65C1,1, 0x65C3,1, 0x65C5,1, 0x65CB,1, 0x65CF,1, 0x65D7,1, 0x65D9,1, 0x65DB,1, 0x65E0,1, 0x65E2,1, 0x65E5,5, 0x65EC,2, 0x65F1,1, 0x65FA,1, 0x6602,1, 0x6606,2, 0x660A,1, 0x660C,1, 0x660E,2, 0x6613,2, 0x661C,1, 0x661F,2, 0x6625,1, 0x6627,2, 0x662D,1, 0x662F,1, 0x6634,2, 0x663C,1, 0x663F,1, 0x6642,3, 0x664B,1, 0x664F,1, 0x6652,1, 0x665D,3, 0x6662,1, 0x6664,1, 0x6666,1, 0x6668,2, 0x666E,3, 0x6674,1, 0x6676,1, 0x667A,1, 0x6681,1, 0x6684,1, 0x6687,3, 0x668E,1, 0x6691,1, 0x6696,3, 0x669D,1, 0x66A2,1, 0x66A6,1, 0x66AB,1, 0x66AE,1, 0x66B4,1, 0x66B8,1, 0x66C4,1, 0x66C7,1, 0x66C9,1, 0x66D6,1, 0x66D9,2, 0x66DC,2, 0x66F0,1, 0x66F2,4, 0x66F7,3, 0x66FC,5, 0x6703,1, 0x6708,2, 0x670B,1, 0x670D,1, 0x670F,1, 0x6714,4, 0x671B,1, 0x671D,1, 0x671F,1, 0x6726,3, 0x672A,5, 0x6731,1, 0x6734,1, 0x6738,1, 0x673A,1, 0x673D,1, 0x673F,1, 0x6741,1, 0x6746,1, 0x6749,1, 0x674E,4, 0x6753,1, 0x6756,1, 0x675C,1, 0x675E,8, 0x676A,1, 0x676D,1, 0x676F,1, 0x6771,1, 0x6773,1, 0x6775,1, 0x6777,1, 0x677C,1, 0x677E,2, 0x6787,1, 0x6789,1, 0x678B,2, 0x6790,1, 0x6795,1, 0x6797,1, 0x679A,1, 0x679C,2, 0x67A0,3, 0x67A6,1, 0x67AF,1, 0x67B4,1, 0x67B6,4, 0x67C1,1, 0x67C4,1, 0x67C6,1, 0x67CA,1, 0x67CE,4, 0x67D3,2, 0x67D8,1, 0x67DA,1, 0x67DD,2, 0x67E2,1, 0x67E4,1, 0x67E7,1, 0x67E9,1, 0x67EC,1, 0x67EE,2, 0x67F1,1, 0x67F3,3, 0x67FB,1, 0x67FE,2, 0x6802,3, 0x6813,1, 0x6816,2, 0x681E,1, 0x6821,2, 0x682A,2, 0x6832,1, 0x6834,1, 0x6838,2, 0x683C,2, 0x6841,3, 0x6848,1, 0x684D,2, 0x6850,2, 0x6853,2, 0x6859,1, 0x685C,2, 0x685F,1, 0x6867,1, 0x6876,1, 0x687F,1, 0x6881,1, 0x6885,1, 0x6893,1, 0x6897,1, 0x689B,1, 0x689D,1, 0x689F,2, 0x68A2,1, 0x68A6,3, 0x68AD,1, 0x68AF,3, 0x68B3,1, 0x68B5,2, 0x68B9,2, 0x68BC,1, 0x68C4,1, 0x68C6,1, 0x68C9,1, 0x68CB,1, 0x68CD,1, 0x68D2,1, 0x68D4,2, 0x68D7,2, 0x68DA,1, 0x68DF,3, 0x68EE,2, 0x68F2,1, 0x68F9,2, 0x6900,2, 0x6904,2, 0x6908,1, 0x690B,5, 0x6912,1, 0x6919,4, 0x6922,1, 0x6925,1, 0x6928,1, 0x6930,1, 0x6934,1, 0x6939,1, 0x693D,1, 0x693F,1, 0x694A,1, 0x6953,3, 0x695A,1, 0x695C,2, 0x6960,3, 0x696A,2, 0x696D,3, 0x6973,3, 0x6977,3, 0x697C,2, 0x6982,1, 0x698A,1, 0x698E,1, 0x6991,1, 0x6994,2, 0x699B,2, 0x69A0,1, 0x69AE,1, 0x69B4,1, 0x69BE,2, 0x69C1,1, 0x69C3,1, 0x69C7,1, 0x69CB,4, 0x69D0,1, 0x69D8,2, 0x69DE,1, 0x69E7,1, 0x69EB,1, 0x69ED,1, 0x69FB,1, 0x69FD,1, 0x69FF,1, 0x6A02,1, 0x6A05,1, 0x6A0B,1, 0x6A13,1, 0x6A17,1, 0x6A19,1, 0x6A1B,1, 0x6A1E,2, 0x6A21,1, 0x6A23,1, 0x6A29,3, 0x6A35,2, 0x6A38,3, 0x6A3D,1, 0x6A44,1, 0x6A47,2, 0x6A4B,1, 0x6A58,2, 0x6A5F,1, 0x6A61,1, 0x6A66,1, 0x6A72,1, 0x6A7F,2, 0x6A84,1, 0x6A8D,2, 0x6A90,1, 0x6A97,1, 0x6A9C,1, 0x6AA2,1, 0x6AAC,1, 0x6AAE,1, 0x6AB3,1, 0x6AB8,1, 0x6ABB,1, 0x6AC2,2, 0x6AD3,1, 0x6ADA,2, 0x6ADE,2, 0x6AE8,1, 0x6AFB,1, 0x6B04,2, 0x6B0A,1, 0x6B12,1, 0x6B1D,1, 0x6B20,2, 0x6B23,1, 0x6B27,1, 0x6B32,1, 0x6B3A,1, 0x6B3D,2, 0x6B4C,1, 0x6B4E,1, 0x6B50,1, 0x6B53,1, 0x6B62,3, 0x6B66,1, 0x6B69,2, 0x6B6F,1, 0x6B73,2, 0x6B79,1, 0x6B7B,1, 0x6B7F,2, 0x6B84,1, 0x6B86,1, 0x6B89,3, 0x6B95,2, 0x6B9E,1, 0x6BAB,1, 0x6BAF,1, 0x6BB1,5, 0x6BB7,1, 0x6BBA,2, 0x6BBF,2, 0x6BC5,2, 0x6BCB,1, 0x6BCD,2, 0x6BD2,1, 0x6BD4,1, 0x6BD8,1, 0x6BDB,1, 0x6BEB,2, 0x6BEF,1, 0x6BF3,1, 0x6C0F,1, 0x6C11,1, 0x6C13,2, 0x6C17,1, 0x6C23,1, 0x6C34,1, 0x6C37,2, 0x6C3E,1, 0x6C40,3, 0x6C4E,1, 0x6C50,1, 0x6C55,1, 0x6C57,1, 0x6C5A,1, 0x6C5D,4, 0x6C62,1, 0x6C6A,1, 0x6C70,1, 0x6C72,2, 0x6C7A,1, 0x6C7D,2, 0x6C81,1, 0x6C83,1, 0x6C88,1, 0x6C8C,1, 0x6C90,1, 0x6C93,1, 0x6C96,1, 0x6C99,2, 0x6CA1,2, 0x6CAB,1, 0x6CAE,1, 0x6CB1,1, 0x6CB3,1, 0x6CB8,8, 0x6CC1,1, 0x6CC4,1, 0x6CC9,2, 0x6CCC,1, 0x6CD3,1, 0x6CD5,1, 0x6CD7,1, 0x6CDB,1, 0x6CDD,1, 0x6CE1,3, 0x6CE5,1, 0x6CE8,1, 0x6CEA,1, 0x6CEF,3, 0x6CF3,1, 0x6D0B,1, 0x6D12,1, 0x6D17,1, 0x6D19,1, 0x6D1B,1, 0x6D1E,2, 0x6D25,1, 0x6D29,2, 0x6D32,1, 0x6D35,1, 0x6D38,1, 0x6D3B,1, 0x6D3D,2, 0x6D41,1, 0x6D44,2, 0x6D59,2, 0x6D5C,1, 0x6D63,1, 0x6D66,1, 0x6D69,2, 0x6D6C,1, 0x6D6E,1, 0x6D74,1, 0x6D77,3, 0x6D85,1, 0x6D88,1, 0x6D8C,1, 0x6D8E,1, 0x6D93,1, 0x6D95,1, 0x6D99,1, 0x6D9B,2, 0x6DAF,1, 0x6DB2,1, 0x6DB5,1, 0x6DB8,1, 0x6DBC,1, 0x6DC0,1, 0x6DC7,1, 0x6DCB,1, 0x6DD1,2, 0x6DD5,1, 0x6DD8,2, 0x6DE1,1, 0x6DE8,1, 0x6DEA,3, 0x6DEE,1, 0x6DF1,1, 0x6DF3,1, 0x6DF5,1, 0x6DF7,1, 0x6DF9,1, 0x6DFB,1, 0x6E05,1, 0x6E07,5, 0x6E13,1, 0x6E15,1, 0x6E19,3, 0x6E20,2, 0x6E23,4, 0x6E29,1, 0x6E2B,3, 0x6E2F,1, 0x6E38,1, 0x6E3A,1, 0x6E3E,1, 0x6E4A,1, 0x6E4D,2, 0x6E56,1, 0x6E58,1, 0x6E5B,2, 0x6E5F,1, 0x6E67,1, 0x6E6B,1, 0x6E6E,2, 0x6E7E,3, 0x6E82,1, 0x6E8C,1, 0x6E8F,2, 0x6E96,1, 0x6E9C,2, 0x6E9F,1, 0x6EA2,1, 0x6EA5,1, 0x6EB2,1, 0x6EB6,1, 0x6EBA,1, 0x6EC2,1, 0x6EC4,2, 0x6EC9,1, 0x6ECB,2, 0x6ED1,1, 0x6ED5,1, 0x6EDD,2, 0x6EEF,1, 0x6EF2,1, 0x6EF4,1, 0x6EF8,1, 0x6EFF,1, 0x6F01,2, 0x6F06,1, 0x6F09,1, 0x6F0F,1, 0x6F11,1, 0x6F13,3, 0x6F20,1, 0x6F22,2, 0x6F2B,2, 0x6F31,1, 0x6F38,1, 0x6F3F,1, 0x6F45,1, 0x6F54,1, 0x6F58,1, 0x6F5B,2, 0x6F5F,1, 0x6F64,1, 0x6F66,1, 0x6F6D,2, 0x6F70,1, 0x6F74,1, 0x6F7C,1, 0x6F81,1, 0x6F84,1, 0x6F86,1, 0x6F8E,1, 0x6F97,1, 0x6FA1,1, 0x6FA4,1, 0x6FAA,1, 0x6FB1,1, 0x6FB3,1, 0x6FB9,1, 0x6FC0,2, 0x6FC3,1, 0x6FC6,1, 0x6FD4,1, 0x6FDB,1, 0x6FE0,2, 0x6FE4,1, 0x6FEB,1, 0x6FEF,1, 0x6FF1,1, 0x6FFE,1, 0x700B,1, 0x700F,1, 0x7011,1, 0x7015,1, 0x7018,1, 0x701D,3, 0x7026,2, 0x702C,1, 0x703E,1, 0x704C,1, 0x7058,1, 0x7063,1, 0x706B,1, 0x706F,2, 0x7078,1, 0x707C,2, 0x7089,2, 0x708E,1, 0x7092,1, 0x7099,1, 0x70AC,3, 0x70B8,3, 0x70C8,1, 0x70CF,1, 0x70D9,1, 0x70DD,1, 0x70F9,1, 0x70FD,1, 0x7109,1, 0x7114,1, 0x7119,2, 0x7121,1, 0x7126,1, 0x7136,1, 0x713C,1, 0x7149,1, 0x714C,1, 0x714E,1, 0x7155,1, 0x7159,1, 0x7164,2, 0x7167,1, 0x7169,1, 0x716C,1, 0x716E,1, 0x717D,1, 0x7184,1, 0x718A,1, 0x718F,1, 0x7194,1, 0x7199,1, 0x719F,1, 0x71A8,1, 0x71B1,1, 0x71BE,1, 0x71C3,1, 0x71C8,1, 0x71CE,1, 0x71D0,1, 0x71D2,1, 0x71D4,2, 0x71D7,1, 0x71E0,1, 0x71E5,3, 0x71ED,1, 0x71F5,1, 0x71FB,2, 0x71FF,1, 0x7206,1, 0x720D,1, 0x7210,1, 0x721B,1, 0x722A,1, 0x722C,2, 0x7232,1, 0x7235,2, 0x723A,2, 0x723D,3, 0x7246,3, 0x724C,1, 0x7252,1, 0x7259,1, 0x725B,1, 0x725D,1, 0x725F,1, 0x7261,2, 0x7267,1, 0x7269,1, 0x7272,1, 0x7274,1, 0x7279,1, 0x727D,2, 0x7280,1, 0x7292,1, 0x72A0,1, 0x72AC,1, 0x72AF,1, 0x72B6,1, 0x72C2,3, 0x72C6,1, 0x72CE,1, 0x72D0,1, 0x72D2,1, 0x72D7,1, 0x72D9,1, 0x72DB,1, 0x72E0,3, 0x72E9,1, 0x72EC,2, 0x72F7,3, 0x72FC,2, 0x730A,1, 0x7316,2, 0x731B,3, 0x731F,1, 0x7325,1, 0x7329,3, 0x732E,2, 0x7334,1, 0x7336,2, 0x733E,2, 0x7344,2, 0x734F,1, 0x7363,1, 0x7368,1, 0x736A,1, 0x7370,1, 0x7372,1, 0x7375,1, 0x737A,1, 0x7384,1, 0x7387,1, 0x7389,1, 0x738B,1, 0x7396,1, 0x73A9,1, 0x73B2,1, 0x73BB,1, 0x73C0,1, 0x73C2,1, 0x73C8,1, 0x73CA,1, 0x73CD,2, 0x73DE,1, 0x73E0,1, 0x73EA,1, 0x73ED,1, 0x73F8,1, 0x73FE,1, 0x7403,1, 0x7405,2, 0x7409,1, 0x7422,1, 0x7425,1, 0x7432,5, 0x743A,1, 0x743F,1, 0x7455,1, 0x7459,4, 0x745E,1, 0x7460,1, 0x7463,2, 0x746A,1, 0x746F,2, 0x7473,1, 0x7476,1, 0x747E,1, 0x7483,1, 0x748B,1, 0x749E,1, 0x74A7,1, 0x74B0,1, 0x74BD,1, 0x74CA,1, 0x74CF,1, 0x74D4,1, 0x74DC,1, 0x74E2,1, 0x74E6,1, 0x74F6,1, 0x750C,1, 0x7511,1, 0x7515,1, 0x7518,1, 0x751A,1, 0x751C,1, 0x751F,1, 0x7523,1, 0x7525,2, 0x7528,1, 0x752B,2, 0x7530,4, 0x7537,1, 0x753A,2, 0x7544,1, 0x7546,1, 0x7549,1, 0x754B,2, 0x754F,1, 0x7551,1, 0x7554,1, 0x7559,1, 0x755B,3, 0x7560,1, 0x7562,1, 0x7564,3, 0x756A,2, 0x756D,1, 0x7570,1, 0x7573,2, 0x7576,3, 0x757F,1, 0x7586,2, 0x758A,2, 0x758E,2, 0x7591,1, 0x75AB,1, 0x75B1,2, 0x75B5,1, 0x75B8,2, 0x75BC,3, 0x75C5,1, 0x75C7,1, 0x75CD,1, 0x75D2,1, 0x75D4,2, 0x75D8,2, 0x75DB,1, 0x75E2,2, 0x75E9,1, 0x75F0,1, 0x75F4,1, 0x75FA,1, 0x760D,1, 0x7621,1, 0x7624,1, 0x7634,1, 0x7642,1, 0x7647,1, 0x764C,1, 0x7652,1, 0x7656,1, 0x766A,1, 0x766C,1, 0x7672,1, 0x7676,1, 0x7678,1, 0x767A,2, 0x767D,2, 0x7684,1, 0x7686,3, 0x768E,1, 0x7690,1, 0x7693,1, 0x7696,1, 0x769A,1, 0x76AE,1, 0x76B0,1, 0x76BA,1, 0x76BF,1, 0x76C2,2, 0x76C6,1, 0x76C8,1, 0x76CA,1, 0x76CD,1, 0x76D2,1, 0x76D7,1, 0x76DB,1, 0x76DF,1, 0x76E1,1, 0x76E3,3, 0x76E7,1, 0x76EE,1, 0x76F2,1, 0x76F4,1, 0x76F8,1, 0x76FE,1, 0x7701,1, 0x7707,3, 0x770B,2, 0x771B,1, 0x771E,3, 0x7724,2, 0x7729,1, 0x7737,2, 0x773A,1, 0x773C,1, 0x7740,1, 0x7747,1, 0x775A,2, 0x7761,1, 0x7763,1, 0x7765,2, 0x7768,1, 0x776B,1, 0x777E,2, 0x778B,1, 0x7791,1, 0x779E,1, 0x77A0,1, 0x77A5,1, 0x77AC,2, 0x77B0,1, 0x77B3,1, 0x77B6,1, 0x77B9,1, 0x77BB,2, 0x77C7,1, 0x77DB,2, 0x77E2,2, 0x77E5,1, 0x77E7,1, 0x77E9,1, 0x77ED,3, 0x77F3,1, 0x77FC,1, 0x7802,1, 0x7812,1, 0x7814,2, 0x7820,1, 0x7825,3, 0x7832,1, 0x7834,1, 0x783A,1, 0x783F,1, 0x7845,1, 0x785D,1, 0x786B,2, 0x786F,1, 0x7872,1, 0x787C,1, 0x7881,1, 0x7887,1, 0x788C,3, 0x7891,1, 0x7893,1, 0x7895,1, 0x7897,1, 0x789A,1, 0x78A3,1, 0x78A7,1, 0x78A9,2, 0x78B5,1, 0x78BA,1, 0x78BC,1, 0x78C1,1, 0x78C5,2, 0x78CA,2, 0x78D0,2, 0x78D4,1, 0x78DA,1, 0x78E7,2, 0x78EC,1, 0x78EF,1, 0x78F4,1, 0x78FD,1, 0x7901,1, 0x7907,1, 0x790E,1, 0x7912,1, 0x7919,1, 0x792A,3, 0x793A,1, 0x793C,1, 0x793E,1, 0x7940,2, 0x7947,3, 0x7950,1, 0x7953,1, 0x7955,3, 0x795A,1, 0x795D,4, 0x7962,1, 0x7965,1, 0x7968,1, 0x796D,1, 0x7977,1, 0x797F,3, 0x7984,2, 0x798A,1, 0x798D,3, 0x79A6,1, 0x79AA,1, 0x79AE,1, 0x79B0,1, 0x79B3,1, 0x79B9,2, 0x79BD,5, 0x79CB,1, 0x79D1,2, 0x79D5,1, 0x79D8,1, 0x79DF,1, 0x79E3,2, 0x79E6,1, 0x79E9,1, 0x79F0,1, 0x79FB,1, 0x7A00,1, 0x7A08,1, 0x7A0B,1, 0x7A0E,1, 0x7A14,1, 0x7A17,4, 0x7A1C,1, 0x7A1F,2, 0x7A2E,1, 0x7A32,1, 0x7A3C,2, 0x7A3F,2, 0x7A42,2, 0x7A46,1, 0x7A4D,4, 0x7A57,1, 0x7A62,2, 0x7A6B,1, 0x7A70,1, 0x7A74,1, 0x7A76,1, 0x7A79,2, 0x7A7D,1, 0x7A7F,1, 0x7A81,1, 0x7A83,2, 0x7A88,1, 0x7A92,2, 0x7A9F,1, 0x7AA9,2, 0x7AAE,2, 0x7ABA,1, 0x7AC3,2, 0x7AC8,1, 0x7ACB,1, 0x7ACF,1, 0x7AD3,1, 0x7AD5,1, 0x7AD9,2, 0x7ADC,2, 0x7ADF,5, 0x7AE5,2, 0x7AEA,1, 0x7AED,1, 0x7AEF,2, 0x7AF6,1, 0x7AF9,2, 0x7AFF,1, 0x7B06,1, 0x7B08,1, 0x7B0F,1, 0x7B11,1, 0x7B19,1, 0x7B1B,1, 0x7B20,1, 0x7B25,2, 0x7B2C,1, 0x7B39,1, 0x7B46,1, 0x7B48,2, 0x7B4B,1, 0x7B4D,1, 0x7B4F,4, 0x7B54,1, 0x7B56,1, 0x7B5D,1, 0x7B6C,1, 0x7B75,1, 0x7B86,2, 0x7B8B,1, 0x7B92,1, 0x7B94,2, 0x7B97,1, 0x7B9D,1, 0x7B9F,1, 0x7BA1,1, 0x7BAA,1, 0x7BAD,1, 0x7BB1,1, 0x7BB8,1, 0x7BC0,1, 0x7BC4,1, 0x7BC7,1, 0x7BC9,1, 0x7BCF,1, 0x7BDD,1, 0x7BE0,1, 0x7BE4,1, 0x7BE9,1, 0x7BED,1, 0x7BF3,1, 0x7C12,1, 0x7C17,1, 0x7C21,1, 0x7C2A,1, 0x7C38,1, 0x7C3E,2, 0x7C4C,2, 0x7C50,1, 0x7C56,1, 0x7C60,1, 0x7C64,1, 0x7C73,1, 0x7C7E,1, 0x7C81,3, 0x7C89,1, 0x7C8B,1, 0x7C8D,1, 0x7C90,1, 0x7C92,1, 0x7C95,1, 0x7C97,2, 0x7C9B,1, 0x7C9F,1, 0x7CA1,1, 0x7CA5,1, 0x7CA7,2, 0x7CAD,2, 0x7CB1,3, 0x7CB9,1, 0x7CBD,2, 0x7CC0,1, 0x7CC2,1, 0x7CC5,1, 0x7CCA,1, 0x7CCE,1, 0x7CD2,1, 0x7CD6,1, 0x7CD8,1, 0x7CDE,3, 0x7CE2,1, 0x7CE7,1, 0x7CEF,1, 0x7CF2,1, 0x7CF8,1, 0x7CFA,2, 0x7CFE,1, 0x7D00,1, 0x7D02,1, 0x7D04,3, 0x7D0B,1, 0x7D0D,1, 0x7D10,1, 0x7D14,2, 0x7D17,5, 0x7D20,3, 0x7D2B,2, 0x7D2F,2, 0x7D32,2, 0x7D35,1, 0x7D39,2, 0x7D3F,1, 0x7D42,3, 0x7D46,1, 0x7D4B,2, 0x7D4E,1, 0x7D50,1, 0x7D56,1, 0x7D5E,1, 0x7D61,3, 0x7D66,1, 0x7D68,1, 0x7D71,2, 0x7D75,2, 0x7D79,1, 0x7D7D,1, 0x7D89,1, 0x7D8F,1, 0x7D93,1, 0x7D99,2, 0x7D9C,1, 0x7D9F,1, 0x7DA2,1, 0x7DAC,2, 0x7DB0,3, 0x7DB4,1, 0x7DB8,1, 0x7DBA,2, 0x7DBD,3, 0x7DCA,2, 0x7DCF,1, 0x7DD1,2, 0x7DD8,1, 0x7DDA,1, 0x7DDD,2, 0x7DE0,1, 0x7DE4,1, 0x7DE8,2, 0x7DEC,1, 0x7DEF,1, 0x7DF2,1, 0x7DF4,1, 0x7DFB,1, 0x7E01,1, 0x7E04,1, 0x7E0A,2, 0x7E12,1, 0x7E1B,1, 0x7E1E,1, 0x7E22,2, 0x7E26,1, 0x7E2B,1, 0x7E2E,1, 0x7E31,2, 0x7E35,1, 0x7E37,1, 0x7E39,2, 0x7E3D,2, 0x7E41,1, 0x7E4A,2, 0x7E4D,1, 0x7E54,3, 0x7E59,2, 0x7E5D,2, 0x7E6A,1, 0x7E6D,1, 0x7E70,1, 0x7E79,1, 0x7E7B,3, 0x7E7F,1, 0x7E82,1, 0x7E89,1, 0x7E8C,1, 0x7E8E,2, 0x7E93,1, 0x7E96,1, 0x7F36,1, 0x7F38,1, 0x7F50,2, 0x7F54,1, 0x7F60,1, 0x7F6A,2, 0x7F6E,1, 0x7F70,1, 0x7F72,1, 0x7F75,1, 0x7F77,1, 0x7F79,1, 0x7F83,1, 0x7F85,2, 0x7F88,1, 0x7F8A,1, 0x7F8E,1, 0x7F9A,1, 0x7F9D,2, 0x7FA4,1, 0x7FA8,2, 0x7FAE,2, 0x7FB6,1, 0x7FB9,1, 0x7FBD,1, 0x7FC1,1, 0x7FC5,1, 0x7FCC,1, 0x7FD2,1, 0x7FD4,1, 0x7FE0,2, 0x7FEB,1, 0x7FF0,1, 0x7FF3,1, 0x7FFB,2, 0x8000,2, 0x8003,4, 0x800C,1 )
}
object FontInfo_NicoKaku2 {
    // Backend Internal error 対策で、１つのobjectの(?)コード量を制限
    val wcRanges : Array<Int> = arrayOf( 0x8010,1, 0x8012,1, 0x8015,1, 0x8017,1, 0x8033,1, 0x8036,1, 0x803B,1, 0x803D,1, 0x8046,1, 0x804A,1, 0x8052,1, 0x8056,1, 0x8058,1, 0x805A,1, 0x805E,1, 0x8061,2, 0x806F,1, 0x8072,1, 0x8074,1, 0x8076,2, 0x807D,3, 0x8084,4, 0x8089,1, 0x808B,2, 0x8096,1, 0x8098,1, 0x809A,2, 0x809D,1, 0x80A1,2, 0x80A5,1, 0x80A9,2, 0x80AF,1, 0x80B1,2, 0x80B4,1, 0x80BA,1, 0x80C3,2, 0x80C6,1, 0x80CC,1, 0x80CE,1, 0x80D6,1, 0x80D9,3, 0x80DD,2, 0x80E1,1, 0x80E4,2, 0x80EF,1, 0x80F1,1, 0x80F4,1, 0x80F8,1, 0x80FC,2, 0x8102,1, 0x8105,4, 0x810A,1, 0x811A,2, 0x8129,1, 0x812F,1, 0x8131,1, 0x8133,1, 0x8139,1, 0x813E,1, 0x814B,1, 0x814E,1, 0x8150,2, 0x8153,3, 0x8165,2, 0x816B,1, 0x816E,1, 0x8170,2, 0x8178,3, 0x817F,2, 0x8188,1, 0x818A,1, 0x818F,1, 0x8195,1, 0x819A,1, 0x819C,2, 0x81A0,1, 0x81A3,1, 0x81A8,1, 0x81B0,1, 0x81B3,1, 0x81B5,1, 0x81BD,4, 0x81C6,1, 0x81C9,1, 0x81CD,1, 0x81D1,1, 0x81D3,1, 0x81D8,3, 0x81DF,2, 0x81E3,1, 0x81E5,1, 0x81E8,1, 0x81EA,1, 0x81ED,1, 0x81F3,2, 0x81FA,1, 0x81FC,1, 0x8205,1, 0x8207,2, 0x820C,1, 0x820E,1, 0x8210,1, 0x8212,1, 0x8216,3, 0x821B,2, 0x821E,2, 0x822A,1, 0x822C,1, 0x822E,1, 0x8233,1, 0x8235,3, 0x8239,1, 0x8240,1, 0x8247,1, 0x8258,3, 0x825F,1, 0x8264,1, 0x8266,1, 0x8268,1, 0x826B,1, 0x826E,2, 0x8271,2, 0x8276,1, 0x8278,1, 0x827E,1, 0x828B,1, 0x828D,1, 0x8292,1, 0x8299,1, 0x829D,1, 0x82A5,2, 0x82AD,1, 0x82AF,1, 0x82B1,1, 0x82B3,1, 0x82B8,2, 0x82BB,1, 0x82BD,1, 0x82C5,1, 0x82D1,1, 0x82D3,2, 0x82D7,1, 0x82DB,1, 0x82DF,1, 0x82E5,3, 0x82EB,1, 0x82F1,1, 0x82F4,1, 0x82FA,2, 0x8302,1, 0x8304,2, 0x8309,1, 0x830E,1, 0x8317,1, 0x831C,1, 0x8328,1, 0x832B,1, 0x8336,1, 0x8338,2, 0x8340,1, 0x8345,1, 0x8349,2, 0x834F,1, 0x8352,1, 0x8358,1, 0x8377,1, 0x837B,2, 0x8389,2, 0x839E,1, 0x83A2,1, 0x83AB,1, 0x83B1,1, 0x83C5,1, 0x83CA,1, 0x83CC,1, 0x83CE,1, 0x83D3,1, 0x83D6,1, 0x83DC,1, 0x83DF,1, 0x83E9,1, 0x83EB,1, 0x83EF,3, 0x8403,2, 0x840C,1, 0x840E,1, 0x8413,1, 0x8420,1, 0x8429,1, 0x842C,1, 0x8431,1, 0x843D,1, 0x8449,1, 0x844E,1, 0x8457,1, 0x845B,1, 0x8461,1, 0x8463,1, 0x8466,1, 0x846C,2, 0x8471,1, 0x8475,1, 0x847A,1, 0x848B,1, 0x8490,1, 0x8494,1, 0x8499,1, 0x849C,1, 0x849F,1, 0x84B2,1, 0x84B8,1, 0x84BB,2, 0x84BF,1, 0x84C4,1, 0x84C9,1, 0x84CB,1, 0x84D1,1, 0x84DA,1, 0x84EC,1, 0x84EE,1, 0x84FC,1, 0x8500,1, 0x8511,1, 0x8513,1, 0x8517,1, 0x851A,1, 0x8526,1, 0x852D,1, 0x8535,1, 0x853D,1, 0x8541,1, 0x8543,1, 0x8548,3, 0x854E,1, 0x8557,1, 0x8568,3, 0x856D,1, 0x857E,1, 0x8580,1, 0x8584,1, 0x8587,1, 0x858A,1, 0x8594,1, 0x8597,1, 0x8599,1, 0x85A6,1, 0x85A8,5, 0x85AE,2, 0x85C1,1, 0x85C9,1, 0x85CD,1, 0x85CF,1, 0x85DD,1, 0x85E4,1, 0x85E9,2, 0x85F7,1, 0x85F9,1, 0x85FB,1, 0x8602,1, 0x8606,2, 0x860A,1, 0x861A,1, 0x862D,1, 0x864D,2, 0x8650,1, 0x8654,2, 0x865A,1, 0x865C,1, 0x865E,2, 0x866B,1, 0x8671,1, 0x8679,1, 0x867B,1, 0x868A,1, 0x8695,1, 0x86A4,1, 0x86A9,1, 0x86AB,1, 0x86AF,2, 0x86B6,1, 0x86C4,1, 0x86C6,2, 0x86C9,1, 0x86CB,1, 0x86CD,2, 0x86D9,1, 0x86DB,1, 0x86DE,2, 0x86E4,1, 0x86ED,3, 0x86F8,2, 0x86FE,1, 0x8700,1, 0x8702,2, 0x8706,1, 0x870D,1, 0x8718,1, 0x871C,1, 0x8729,1, 0x8734,1, 0x873B,1, 0x873F,1, 0x8749,1, 0x874B,1, 0x874E,1, 0x8755,1, 0x8757,1, 0x8759,1, 0x875F,2, 0x8766,1, 0x876A,1, 0x8774,1, 0x8776,1, 0x8778,1, 0x877F,1, 0x878D,1, 0x879F,1, 0x87A2,1, 0x87BA,2, 0x87C4,1, 0x87C6,2, 0x87D0,1, 0x87E0,1, 0x87EF,1, 0x87F2,1, 0x87F9,1, 0x87FB,1, 0x87FE,1, 0x8805,1, 0x880D,1, 0x8815,2, 0x8822,2, 0x8831,1, 0x883B,1, 0x8840,1, 0x8846,1, 0x884C,2, 0x8853,1, 0x8857,1, 0x8859,1, 0x885B,1, 0x885D,1, 0x8861,1, 0x8863,1, 0x8868,1, 0x886B,1, 0x8870,1, 0x8877,1, 0x887D,3, 0x8881,2, 0x8888,1, 0x888B,1, 0x888D,1, 0x8892,1, 0x8896,2, 0x8899,1, 0x88A2,1, 0x88AB,1, 0x88AE,1, 0x88B1,1, 0x88B4,1, 0x88B7,1, 0x88BF,1, 0x88C1,5, 0x88CF,1, 0x88D4,2, 0x88DC,2, 0x88DF,1, 0x88E1,1, 0x88E8,1, 0x88F2,2, 0x88F8,1, 0x88FC,3, 0x8904,1, 0x8907,1, 0x890A,1, 0x890C,1, 0x8910,1, 0x8912,1, 0x891D,1, 0x8925,1, 0x892A,1, 0x8938,1, 0x893B,1, 0x8944,1, 0x894C,1, 0x8956,1, 0x895E,2, 0x8964,1, 0x8966,1, 0x8972,1, 0x8974,1, 0x8977,1, 0x897E,2, 0x8981,1, 0x8986,2, 0x898B,1, 0x898F,1, 0x8996,2, 0x899A,1, 0x89A7,1, 0x89AA,1, 0x89B3,1, 0x89BA,1, 0x89D2,1, 0x89DA,1, 0x89DC,2, 0x89E3,1, 0x89E6,2, 0x89F8,1, 0x8A00,1, 0x8A02,2, 0x8A08,1, 0x8A0A,1, 0x8A0C,1, 0x8A0E,1, 0x8A10,1, 0x8A13,1, 0x8A16,3, 0x8A1B,1, 0x8A1D,1, 0x8A1F,1, 0x8A23,1, 0x8A2A,1, 0x8A2D,1, 0x8A31,1, 0x8A33,2, 0x8A36,1, 0x8A3A,3, 0x8A41,1, 0x8A46,1, 0x8A48,1, 0x8A50,3, 0x8A54,2, 0x8A5B,1, 0x8A5E,1, 0x8A60,1, 0x8A62,2, 0x8A66,1, 0x8A69,1, 0x8A6B,1, 0x8A6D,2, 0x8A70,4, 0x8A82,1, 0x8A85,1, 0x8A87,1, 0x8A89,1, 0x8A8C,2, 0x8A93,1, 0x8A95,1, 0x8A98,1, 0x8A9E,1, 0x8AA0,1, 0x8AA3,2, 0x8AA6,1, 0x8AAC,2, 0x8AB0,1, 0x8AB2,1, 0x8AB9,1, 0x8ABC,1, 0x8ABF,1, 0x8AC2,1, 0x8AC4,1, 0x8AC7,1, 0x8ACB,3, 0x8ACF,1, 0x8AD2,1, 0x8AD6,1, 0x8ADA,1, 0x8ADC,1, 0x8ADE,1, 0x8AE0,3, 0x8AE4,1, 0x8AE6,2, 0x8AEB,1, 0x8AED,2, 0x8AF1,1, 0x8AF7,2, 0x8AFA,1, 0x8AFE,1, 0x8B00,3, 0x8B04,1, 0x8B0E,1, 0x8B17,1, 0x8B19,3, 0x8B1D,1, 0x8B20,2, 0x8B26,1, 0x8B28,1, 0x8B2B,2, 0x8B33,1, 0x8B39,1, 0x8B3E,1, 0x8B41,1, 0x8B49,1, 0x8B4E,2, 0x8B56,1, 0x8B58,1, 0x8B5A,3, 0x8B5F,1, 0x8B66,1, 0x8B6B,1, 0x8B6F,2, 0x8B72,1, 0x8B77,1, 0x8B80,1, 0x8B83,1, 0x8B8A,1, 0x8B8C,1, 0x8B90,1, 0x8B92,2, 0x8B96,1, 0x8B99,2, 0x8C37,1, 0x8C46,1, 0x8C48,1, 0x8C4A,1, 0x8C4C,1, 0x8C55,1, 0x8C5A,1, 0x8C61,1, 0x8C6A,2, 0x8C78,2, 0x8C8A,1, 0x8C8C,3, 0x8C9D,2, 0x8CA0,3, 0x8CA7,6, 0x8CAF,2, 0x8CB3,2, 0x8CB6,3, 0x8CBB,3, 0x8CBF,6, 0x8CC7,1, 0x8CCA,1, 0x8CCD,2, 0x8CD1,1, 0x8CD3,1, 0x8CDB,2, 0x8CDE,1, 0x8CE0,1, 0x8CE2,3, 0x8CE6,1, 0x8CEA,1, 0x8CED,1, 0x8CFB,3, 0x8D04,2, 0x8D08,1, 0x8D0A,2, 0x8D0D,1, 0x8D10,1, 0x8D14,1, 0x8D16,1, 0x8D64,1, 0x8D66,1, 0x8D6B,1, 0x8D70,1, 0x8D73,2, 0x8D77,1, 0x8D85,1, 0x8D8A,1, 0x8D99,1, 0x8DA3,1, 0x8DA8,1, 0x8DB3,1, 0x8DBA,1, 0x8DBE,1, 0x8DC2,1, 0x8DCB,2, 0x8DCF,1, 0x8DD6,1, 0x8DDA,1, 0x8DDD,1, 0x8DDF,1, 0x8DE1,1, 0x8DE3,1, 0x8DE8,1, 0x8DEA,1, 0x8DEF,1, 0x8DF3,1, 0x8DF5,1, 0x8E08,3, 0x8E0F,1, 0x8E1D,3, 0x8E2A,1, 0x8E35,1, 0x8E42,1, 0x8E44,1, 0x8E49,1, 0x8E4C,1, 0x8E59,1, 0x8E5F,1, 0x8E63,2, 0x8E74,1, 0x8E7C,1, 0x8E81,1, 0x8E85,1, 0x8E87,1, 0x8E8A,1, 0x8E8D,1, 0x8E91,1, 0x8E93,2, 0x8E99,1, 0x8EA1,1, 0x8EAB,2, 0x8EAF,3, 0x8EBE,1, 0x8EC6,1, 0x8ECA,4, 0x8ED2,1, 0x8EDF,1, 0x8EE2,1, 0x8EEB,1, 0x8EF8,1, 0x8EFB,4, 0x8F03,1, 0x8F05,1, 0x8F09,2, 0x8F0C,1, 0x8F13,3, 0x8F19,1, 0x8F1B,3, 0x8F1F,1, 0x8F26,1, 0x8F29,2, 0x8F2F,1, 0x8F33,1, 0x8F38,1, 0x8F3B,1, 0x8F3F,1, 0x8F44,1, 0x8F46,1, 0x8F49,1, 0x8F4D,2, 0x8F5C,1, 0x8F5F,1, 0x8F61,2, 0x8F64,1, 0x8F9B,2, 0x8F9E,2, 0x8FA3,1, 0x8FB0,3, 0x8FBA,3, 0x8FBF,1, 0x8FC2,1, 0x8FC4,2, 0x8FCE,1, 0x8FD1,1, 0x8FD4,1, 0x8FE6,1, 0x8FE9,1, 0x8FEB,1, 0x8FED,1, 0x8FF0,1, 0x8FF7,1, 0x8FFD,1, 0x9000,2, 0x9003,1, 0x9005,2, 0x900F,2, 0x9013,2, 0x9017,1, 0x9019,2, 0x901D,7, 0x902E,1, 0x9031,2, 0x9038,1, 0x903C,1, 0x9041,2, 0x9045,1, 0x9047,1, 0x904A,2, 0x904D,2, 0x9053,3, 0x9059,1, 0x905C,1, 0x9060,2, 0x9063,1, 0x9065,1, 0x9069,1, 0x906D,2, 0x9075,1, 0x9077,2, 0x907A,1, 0x907C,2, 0x907F,1, 0x9081,2, 0x9084,1, 0x9087,1, 0x908A,1, 0x9091,1, 0x90A3,1, 0x90A6,1, 0x90AA,1, 0x90B1,1, 0x90B8,1, 0x90C1,1, 0x90CA,1, 0x90CE,1, 0x90DB,1, 0x90E1,1, 0x90E8,1, 0x90ED,1, 0x90F5,1, 0x90F7,1, 0x90FD,1, 0x9102,1, 0x9112,1, 0x9119,1, 0x912D,1, 0x9132,1, 0x9149,6, 0x9152,1, 0x9154,1, 0x9156,1, 0x9158,1, 0x9162,2, 0x9169,2, 0x916C,1, 0x9175,1, 0x9177,2, 0x9182,1, 0x9187,1, 0x9189,1, 0x918B,1, 0x918D,1, 0x9190,1, 0x9192,1, 0x9197,1, 0x919C,1, 0x91A4,1, 0x91AB,1, 0x91B4,1, 0x91B8,1, 0x91BA,1, 0x91C0,1, 0x91C6,4, 0x91CB,5, 0x91D1,1, 0x91D8,1, 0x91DB,3, 0x91DF,1, 0x91E3,1, 0x91E6,2, 0x91F6,1, 0x91FC,1, 0x920D,2, 0x9211,1, 0x9214,2, 0x921E,1, 0x922C,1, 0x9234,1, 0x9237,1, 0x923F,1, 0x9244,1, 0x9248,2, 0x924B,1, 0x9250,1, 0x9257,1, 0x925A,2, 0x925E,1, 0x9262,1, 0x9264,1, 0x9266,1, 0x9271,1, 0x927E,1, 0x9280,1, 0x9283,1, 0x9285,1, 0x9291,1, 0x9295,2, 0x9298,1, 0x929A,2, 0x92AD,1, 0x92B9,1, 0x92CF,1, 0x92D2,1, 0x92E4,1, 0x92EA,1, 0x92ED,1, 0x92F2,2, 0x92F8,1, 0x92FA,1, 0x92FC,1, 0x9306,1, 0x930F,2, 0x9318,1, 0x931A,1, 0x9320,1, 0x9323,1, 0x9326,1, 0x9328,1, 0x932B,2, 0x932E,2, 0x9332,1, 0x933B,1, 0x9344,1, 0x934B,1, 0x934D,1, 0x9354,1, 0x9356,1, 0x935B,2, 0x9360,1, 0x936C,1, 0x936E,1, 0x9375,1, 0x937C,1, 0x937E,1, 0x938C,1, 0x9394,1, 0x9396,2, 0x939A,1, 0x93A7,1, 0x93AC,3, 0x93B0,1, 0x93C8,1, 0x93D1,1, 0x93D7,1, 0x93DD,1, 0x93E1,1, 0x93E4,1, 0x9403,1, 0x9407,1, 0x9410,1, 0x9414,1, 0x9418,3, 0x9435,1, 0x9438,1, 0x9444,1, 0x9451,1, 0x9453,1, 0x945A,1, 0x945E,1, 0x9460,1, 0x946A,1, 0x9475,1, 0x9477,1, 0x947D,3, 0x9577,1, 0x9580,1, 0x9582,2, 0x9589,1, 0x958B,1, 0x958F,1, 0x9591,1, 0x9593,2, 0x9596,1, 0x9598,1, 0x95A2,4, 0x95A7,1, 0x95B2,1, 0x95BB,1, 0x95BE,1, 0x95C7,1, 0x95CA,1, 0x95CC,2, 0x95D6,1, 0x95D8,1, 0x95E2,1, 0x961C,1, 0x9621,1, 0x962A,1, 0x962E,2, 0x9632,1, 0x963B,1, 0x963F,2, 0x9642,1, 0x9644,1, 0x964B,3, 0x964F,2, 0x965B,3, 0x9662,5, 0x966A,1, 0x966C,1, 0x9670,1, 0x9672,2, 0x9675,4, 0x967A,1, 0x967D,1, 0x9685,2, 0x9688,1, 0x968A,2, 0x968D,3, 0x9694,2, 0x9697,3, 0x969B,2, 0x96A0,1, 0x96A3,1, 0x96A7,1, 0x96AA,1, 0x96B4,1, 0x96B6,2, 0x96B9,1, 0x96BB,2, 0x96C0,2, 0x96C4,4, 0x96C9,1, 0x96CC,3, 0x96D1,1, 0x96D6,1, 0x96D9,1, 0x96DB,2, 0x96E2,2, 0x96E8,1, 0x96EA,2, 0x96F0,1, 0x96F2,1, 0x96F6,2, 0x96F9,1, 0x96FB,1, 0x9700,1, 0x9707,1, 0x970A,1, 0x970D,1, 0x9716,1, 0x971C,1, 0x971E,1, 0x9727,1, 0x9730,1, 0x9732,1, 0x9739,1, 0x9742,1, 0x9744,1, 0x9748,1, 0x9752,1, 0x9756,1, 0x9759,1, 0x975C,1, 0x975E,1, 0x9761,2, 0x9768,2, 0x976D,1, 0x9771,1, 0x9774,1, 0x977A,1, 0x977C,1, 0x9784,1, 0x9786,1, 0x978B,1, 0x978D,1, 0x9790,1, 0x9798,1, 0x979C,1, 0x97A0,1, 0x97A3,1, 0x97A6,1, 0x97A8,1, 0x97AD,1, 0x97B3,2, 0x97C3,1, 0x97CB,1, 0x97D3,1, 0x97DC,1, 0x97ED,2, 0x97F3,1, 0x97FB,1, 0x97FF,1, 0x9801,3, 0x9805,2, 0x9808,1, 0x980C,1, 0x9810,4, 0x9817,2, 0x981A,1, 0x982C,2, 0x9834,1, 0x9837,2, 0x983B,3, 0x9846,1, 0x984C,3, 0x9854,2, 0x9858,1, 0x985B,1, 0x985E,1, 0x9867,1, 0x9870,1, 0x98A8,1, 0x98AA,1, 0x98AF,1, 0x98B1,1, 0x98C4,1, 0x98DB,2, 0x98DF,1, 0x98E2,1, 0x98E9,1, 0x98EB,1, 0x98EE,2, 0x98F2,1, 0x98F4,1, 0x98FC,3, 0x9903,1, 0x9905,1, 0x9909,2, 0x990C,1, 0x9910,1, 0x9913,1, 0x9918,1, 0x991E,1, 0x9921,1, 0x9928,1, 0x9942,1, 0x9945,1, 0x9949,1, 0x994B,1, 0x9952,1, 0x9957,1, 0x9996,1, 0x9999,1, 0x99A8,1, 0x99AC,3, 0x99B3,2, 0x99C1,1, 0x99C4,3, 0x99C8,1, 0x99D0,1, 0x99D2,1, 0x99D5,1, 0x99D8,1, 0x99DD,1, 0x99ED,2, 0x99F1,2, 0x99FB,1, 0x99FF,1, 0x9A01,1, 0x9A0E,1, 0x9A12,2, 0x9A19,1, 0x9A28,1, 0x9A2B,1, 0x9A30,1, 0x9A3E,1, 0x9A43,1, 0x9A45,1, 0x9A4D,1, 0x9A55,1, 0x9A57,1, 0x9A5A,2, 0x9A62,1, 0x9A64,1, 0x9A69,1, 0x9A6B,1, 0x9AA8,1, 0x9AB8,1, 0x9AC0,1, 0x9AC4,1, 0x9ACF,1, 0x9AD1,1, 0x9AD4,1, 0x9AD8,2, 0x9ADF,1, 0x9AEA,1, 0x9AED,1, 0x9AF7,1, 0x9B06,1, 0x9B25,1, 0x9B28,1, 0x9B2F,1, 0x9B31,2, 0x9B3C,1, 0x9B41,5, 0x9B4D,3, 0x9B51,1, 0x9B54,1, 0x9B5A,1, 0x9B6F,1, 0x9B8E,1, 0x9B91,3, 0x9B96,2, 0x9B9F,2, 0x9BA8,1, 0x9BAA,2, 0x9BAD,2, 0x9BC6,1, 0x9BC9,1, 0x9BD1,2, 0x9BD6,1, 0x9BDB,1, 0x9BE3,1, 0x9BE8,1, 0x9BF0,2, 0x9BF5,1, 0x9C06,1, 0x9C08,3, 0x9C0D,1, 0x9C10,1, 0x9C13,2, 0x9C21,1, 0x9C24,1, 0x9C2D,1, 0x9C2F,1, 0x9C39,3, 0x9C3E,1, 0x9C46,3, 0x9C52,1, 0x9C57,1, 0x9C5A,1, 0x9C60,1, 0x9C67,1, 0x9C78,1, 0x9CE5,1, 0x9CE9,1, 0x9CF3,2, 0x9CF6,1, 0x9D07,1, 0x9D09,1, 0x9D0E,1, 0x9D1B,1, 0x9D26,1, 0x9D28,1, 0x9D2B,2, 0x9D3B,1, 0x9D5C,1, 0x9D5E,1, 0x9D60,2, 0x9D6C,1, 0x9D6F,1, 0x9D7A,1, 0x9D89,1, 0x9D8F,1, 0x9D9A,1, 0x9DAF,1, 0x9DB4,1, 0x9DED,1, 0x9DF2,1, 0x9DF9,2, 0x9DFD,1, 0x9E1E,1, 0x9E75,1, 0x9E78,2, 0x9E7F,1, 0x9E92,2, 0x9E97,1, 0x9E9F,1, 0x9EA5,2, 0x9EA9,1, 0x9EB9,3, 0x9EBE,2, 0x9EC4,1, 0x9ECD,2, 0x9ED2,1, 0x9ED9,1, 0x9EDB,2, 0x9EDE,1, 0x9EF9,1, 0x9EFD,1, 0x9F0E,1, 0x9F13,1, 0x9F20,2, 0x9F3B,1, 0x9F4A,2, 0x9F4E,1, 0x9F52,1, 0x9F5F,1, 0x9F62,1, 0x9F67,1, 0x9F6A,1, 0x9F6C,1, 0x9F77,1, 0x9F8D,1, 0x9F9C,1, 0x9FA0,1, 0xFA11,1, 0xFF01,6, 0xFF08,56, 0xFF41,30, 0xFF61,63, 0xFFE0,3, 0xFFE4,2 )
}