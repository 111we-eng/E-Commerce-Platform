/**
 * 商品 Mock 数据 — 当后端不可用时自动生效
 *
 * 模拟真实的异步分页请求：每次调用返回一页数据
 * 包含网络延迟模拟（200-600ms），使开发和测试更接近真实环境
 */

// ===================== Mock 商品数据池（30条） =====================

const allProducts = [
  { id: 1,  name: 'MacBook Pro 14寸',     category: '电子产品', price: 14999.00, stock: 50, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i3/2200721531555/O1CN01wRlRqP1mVqNwXW3ZL_!!2200721531555.jpg',
    description: 'Apple M3 Pro芯片 / 18GB内存 / 512GB固态硬盘 / Liquid Retina XDR显示屏 / 长达17小时电池续航' },
  { id: 2,  name: 'iPhone 16 Pro',         category: '电子产品', price: 8999.00,  stock: 100, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i4/2211765384132/O1CN01kPlWPW1rMCEvJXg3H_!!2211765384132.jpg',
    description: 'A18 Pro芯片 / 256GB存储 / 钛金属设计 / 4800万像素主摄 / 支持USB-C' },
  { id: 3,  name: 'AirPods Pro 3',         category: '电子产品', price: 1899.00,  stock: 200, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i2/2207957235079/O1CN01UxKXrJ2GRBKoBPwlU_!!2207957235079.jpg',
    description: '自适应降噪 / 空间音频 / 支持USB-C充电 / 6小时续航 / IPX4防水' },
  { id: 4,  name: '华为 MatePad Pro',       category: '电子产品', price: 4299.00,  stock: 60, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i2/2200721531555/O1CN01w4sRNW1mVqNumBx33_!!2200721531555.jpg',
    description: '12.6寸OLED屏 / 麒麟9000S / 12GB+256GB / 支持M-Pencil / HarmonyOS' },
  { id: 5,  name: '索尼 WH-1000XM6',        category: '电子产品', price: 2499.00,  stock: 120, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i1/2200721531555/O1CN01fLVXMp1mVqNtjJRY2_!!2200721531555.jpg',
    description: '行业标杆降噪 / 30小时续航 / Hi-Res Audio认证 / 轻量化仅250g' },
  { id: 6,  name: '戴森 V16 吸尘器',        category: '生活家电', price: 3999.00,  stock: 40, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i3/2200721531555/O1CN01bUKVUJ1mVqNx6JJ0z_!!2200721531555.jpg',
    description: '数字马达250AW吸力 / 激光探测微尘 / 60分钟续航 / 整机HEPA过滤' },
  { id: 7,  name: '小米空气净化器 4 Pro',    category: '生活家电', price: 1999.00,  stock: 80, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i2/2200721531555/O1CN01abcdef1mVqNx6JJ0z_!!2200721531555.jpg',
    description: 'CADR值500m³/h / 适用面积60㎡ / OLED触控屏 / 米家APP远程控制' },
  { id: 8,  name: '科沃斯 X2 扫地机器人',    category: '生活家电', price: 4599.00,  stock: 35, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i3/2200721531555/O1CN01ghijkl1mVqNx6JJ0z_!!2200721531555.jpg',
    description: '全能基站 / 自动集尘/洗拖布/烘干 / 8000Pa吸力 / AI避障3.0' },
  { id: 9,  name: '石头 P10 Pro',           category: '生活家电', price: 3299.00,  stock: 55, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i4/2200721531555/O1CN01mnopqr1mVqNx6JJ0z_!!2200721531555.jpg',
    description: '双旋转拖布 / 5500Pa吸力 / 自动补水 / 基站自清洁 / 60天免维护' },
  { id: 10, name: '追觅 H30 洗地机',         category: '生活家电', price: 2699.00,  stock: 65, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i1/2200721531555/O1CN01stuvwx1mVqNx6JJ0z_!!2200721531555.jpg',
    description: '17000Pa吸力 / 滚刷双侧贴边 / 电解水除菌 / 自清洁+烘干 / LED显示屏' },
  { id: 11, name: 'Nike Air Max 270',       category: '运动鞋服', price: 899.00,   stock: 85, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i3/2219470023707/O1CN01sHDqvC1eWBNv6H4W6_!!2219470023707.jpg',
    description: '经典气垫跑鞋 / 透气网面鞋面 / 耐磨橡胶外底 / 270度大气垫缓震' },
  { id: 12, name: 'Adidas Ultraboost 23',   category: '运动鞋服', price: 1099.00,  stock: 72, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i2/2219470023707/O1CN01PI28GQ1eWBNwwJg0A_!!2219470023707.jpg',
    description: 'BOOST中底科技 / Primeknit+编织鞋面 / Continental橡胶 / 袜套式设计' },
  { id: 13, name: '安踏 C37 4.0',           category: '运动鞋服', price: 499.00,   stock: 150, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i3/2219470023707/O1CN01vwxyz11eWBNwwJg0A_!!2219470023707.jpg',
    description: 'C37科技中底 / 轻弹缓震 / 贾卡透气鞋面 / 国产旗舰跑鞋' },
  { id: 14, name: '李宁 超轻21',             category: '运动鞋服', price: 599.00,   stock: 110, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i4/2219470023707/O1CN01abcdef1eWBNwwJg0A_!!2219470023707.jpg',
    description: '䨻科技中底 / 单只仅170g / 透气MONO纱 / 全掌碳板支撑' },
  { id: 15, name: 'The North Face 冲锋衣',   category: '运动鞋服', price: 1899.00,  stock: 45, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i1/2219470023707/O1CN01ghijkl1eWBNwwJg0A_!!2219470023707.jpg',
    description: 'GTX 3L防水透气面料 / 全压胶工艺 / 可调节风帽 / 腋下透气拉链' },
  { id: 16, name: 'iPad Air M2',            category: '电子产品', price: 4799.00,  stock: 70, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i4/2200721531555/O1CN01wRlRqP1mVqNwXW3ZL_!!2200721531555.jpg',
    description: 'M2芯片 / 10.9寸Liquid Retina / 支持Apple Pencil Pro / 轻薄设计' },
  { id: 17, name: '三星 Galaxy Watch 7',     category: '电子产品', price: 2299.00,  stock: 55, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i2/2200721531555/O1CN01kPlWPW1rMCEvJXg3H_!!2200721531555.jpg',
    description: 'BioActive传感器 / 血压+ECG监测 / 蓝宝石玻璃 / Wear OS 5 / 钛金属版' },
  { id: 18, name: '戴尔 U2723QE 显示器',    category: '电子产品', price: 3499.00,  stock: 38, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i3/2200721531555/O1CN01UxKXrJ2GRBKoBPwlU_!!2200721531555.jpg',
    description: '27寸4K IPS Black / USB-C 90W充电 / 内置KVM / ΔE<2色准 / 3年质保' },
  { id: 19, name: 'Bose SoundLink Max',     category: '电子产品', price: 3299.00,  stock: 45, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i1/2200721531555/O1CN01w4sRNW1mVqNumBx33_!!2200721531555.jpg',
    description: '360°环绕音效 / 20小时续航 / IP67防水防尘 / 可串联立体声' },
  { id: 20, name: '美的 智能变频空调 1.5匹', category: '生活家电', price: 2999.00,  stock: 90, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i3/2200721531555/O1CN01bUKVUJ1mVqNx6JJ0z_!!2200721531555.jpg',
    description: '新一级能效 / 全直流变频 / 智能WIFI控制 / 自清洁 / 30秒速冷' },
  { id: 21, name: '海尔 500L 对开门冰箱',    category: '生活家电', price: 4599.00,  stock: 25, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i2/2200721531555/O1CN01stuvwx1mVqNx6JJ0z_!!2200721531555.jpg',
    description: '风冷无霜 / 双变频 / 干湿分储 / DEO净味 / T·ABT杀菌' },
  { id: 22, name: 'Nike Dunk Low Retro',    category: '运动鞋服', price: 799.00,   stock: 130, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i3/2219470023707/O1CN01sHDqvC1eWBNv6H4W6_!!2219470023707.jpg',
    description: '经典复刻鞋型 / 皮革鞋面 / 低帮设计 / 加厚鞋舌 / 多配色可选' },
  { id: 23, name: '骆驼 户外帐篷 3-4人',     category: '运动鞋服', price: 599.00,   stock: 60, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i1/2219470023707/O1CN01PI28GQ1eWBNwwJg0A_!!2219470023707.jpg',
    description: '3秒速开 / 双层防雨 / UPF50+防晒 / 液压弹簧支架 / 自动速开' },
  { id: 24, name: '九阳 破壁机 P919',        category: '生活家电', price: 899.00,   stock: 140, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i4/2200721531555/O1CN01mnopqr1mVqNx6JJ0z_!!2200721531555.jpg',
    description: '35000转/分 / 8叶刀头 / 12小时预约 / 一键清洗 / 降噪至50dB' },
  { id: 25, name: 'SK-II 神仙水 230ml',     category: '生活家电', price: 1370.00,  stock: 100, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i1/2200721531555/O1CN01fLVXMp1mVqNtjJRY2_!!2200721531555.jpg',
    description: 'PITERA精华 / 改善肤质 / 细致毛孔 / 日本原装进口 / 清爽不油腻' },
  { id: 26, name: 'Surface Laptop 6',       category: '电子产品', price: 9999.00,  stock: 30, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i4/2200721531555/O1CN01wRlRqP1mVqNwXW3ZL_!!2200721531555.jpg',
    description: 'i7-13800H / 16GB+512GB / 15寸PixelSense / 触控屏 / 17小时续航' },
  { id: 27, name: '任天堂 Switch OLED',      category: '电子产品', price: 2199.00,  stock: 95, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i2/2200721531555/O1CN01kPlWPW1rMCEvJXg3H_!!2200721531555.jpg',
    description: '7寸OLED屏 / 64GB存储 / 广色域 / 有线网口底座 / 可拆卸Joy-Con' },
  { id: 28, name: 'Under Armour 训练T恤',    category: '运动鞋服', price: 299.00,   stock: 200, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i4/2219470023707/O1CN01abcdef1eWBNwwJg0A_!!2219470023707.jpg',
    description: 'HeatGear面料 / 速干排汗 / 四向弹力 / 抗菌防臭 / UPF30防晒' },
  { id: 29, name: '苏泊尔 电饭煲 4L',        category: '生活家电', price: 499.00,   stock: 170, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i2/2200721531555/O1CN01stuvwx1mVqNx6JJ0z_!!2200721531555.jpg',
    description: 'IH电磁加热 / 3mm厚釜内胆 / 24小时预约 / 10大功能菜单 / 可拆卸上盖' },
  { id: 30, name: '佳能 EOS R8 微单相机',    category: '电子产品', price: 10499.00, stock: 18, status: 'ON',
    image: 'https://img.alicdn.com/imgextra/i3/2200721531555/O1CN01UxKXrJ2GRBKoBPwlU_!!2200721531555.jpg',
    description: '2420万全画幅CMOS / 40张/秒连拍 / 4K 60p视频 / 仅461g / RF卡口' },
]

// 提取所有分类
const allCategories = [...new Set(allProducts.map(p => p.category))]

// ===================== Mock API =====================

/**
 * 模拟网络延迟（200-600ms 随机）
 */
function simulateDelay() {
  const ms = 200 + Math.random() * 400
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * Mock: 商品分页查询
 *
 * @param {object} params
 *   - page:     页码（默认1）
 *   - pageSize: 每页条数（默认8）
 *   - category: 分类筛选（可选）
 *   - keyword:  关键词搜索（可选）
 * @returns {Promise<{code, message, data: {records, total, current, pages}}>}
 */
export async function mockProductPage(params = {}) {
  await simulateDelay()

  const page     = parseInt(params.page)     || 1
  const pageSize = parseInt(params.pageSize) || 8
  const category = params.category || ''
  const keyword  = params.keyword  || ''

  // 1. 筛选
  let filtered = [...allProducts]

  if (category) {
    filtered = filtered.filter(p => p.category === category)
  }

  if (keyword) {
    filtered = filtered.filter(p =>
      p.name.includes(keyword) || p.description.includes(keyword)
    )
  }

  // 2. 分页
  const total  = filtered.length
  const pages  = Math.ceil(total / pageSize)
  const start  = (page - 1) * pageSize
  const end    = start + pageSize
  const records = filtered.slice(start, end)

  console.log(
    `[Mock] GET /api/products?page=${page}&pageSize=${pageSize}` +
    `&category=${category}&keyword=${keyword}` +
    ` → 返回 ${records.length} 条 / 共 ${total} 条 (第 ${page}/${pages} 页)`
  )

  return {
    code: 200,
    message: '操作成功',
    data: {
      records,
      total,
      current: page,
      pages
    }
  }
}

/**
 * Mock: 商品搜索
 */
export async function mockProductSearch(keyword, page = 1, pageSize = 8) {
  return mockProductPage({ page, pageSize, keyword })
}

/**
 * Mock: 商品详情
 */
export async function mockProductDetail(id) {
  await simulateDelay()
  const product = allProducts.find(p => p.id === parseInt(id))
  if (!product) return { code: 404, message: '商品不存在', data: null }
  return { code: 200, message: '操作成功', data: product }
}

/**
 * Mock: 获取所有分类
 */
export async function mockCategories() {
  await simulateDelay(100)
  return { code: 200, message: '操作成功', data: allCategories }
}

/** 导出 Mock 数据供调试 */
export { allProducts, allCategories }
