//package com.example.handispace.data.repository.user
//
//import com.example.handispace.model.Category
//import com.example.handispace.model.Product
//import com.example.handispace.utils.ResultState
//import com.google.firebase.Timestamp
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.tasks.await
//import javax.inject.Inject
//
//class DataSeeder @Inject constructor(
//    private val db: FirebaseFirestore
//) {
//    suspend fun seedCategoriesAndProducts(): ResultState<Boolean> {
//        return try {
//            val batch = db.batch()
//
//            // =========================================================
//            // 1. KHỞI TẠO 5 DANH MỤC CHUẨN (2 Vật liệu/Dụng cụ, 3 Thành phẩm)
//            // =========================================================
//            val catMaterial1 = Category(category_id = "cat_mat_01", name = "Vật liệu DIY", type = "material", created_at = Timestamp.now())
//            val catMaterial2 = Category(category_id = "cat_mat_02", name = "Dụng cụ chế tác", type = "material", created_at = Timestamp.now())
//            val catProduct1 = Category(category_id = "cat_prod_01", name = "Đồ trang trí thủ công", type = "product", created_at = Timestamp.now())
//            val catProduct2 = Category(category_id = "cat_prod_02", name = "Trang sức Handmade", type = "product", created_at = Timestamp.now())
//            val catProduct3 = Category(category_id = "cat_prod_03", name = "Quà tặng sáng tạo", type = "product", created_at = Timestamp.now())
//
//            val categories = listOf(catMaterial1, catMaterial2, catProduct1, catProduct2, catProduct3)
//            categories.forEach { cat ->
//                val catRef = db.collection("categories").document(cat.category_id)
//                batch.set(catRef, cat)
//            }
//
//            // Link ảnh tạm thời chống mù (Ní thay bằng link Cloudinary sau nhé)
//            val placeholderImg = listOf("https://via.placeholder.com/400x400.png?text=Handispace+Image")
//
//            // =========================================================
//            // 2. KHỞI TẠO 25 SẢN PHẨM (RẢI ĐỀU 5 MÓN/DANH MỤC - CÓ COMBO CHUÔNG GIÓ DIY)
//            // =========================================================
//            val products = mutableListOf<Product>()
//
//            // --- DANH MỤC 1: VẬT LIỆU DIY (Cực kỳ quan trọng để mai test AI gợi ý làm chuông gió) ---
//            products.add(Product(
//                product_id = "prod_01", category_id = catMaterial1.category_id, category_name = catMaterial1.name,
//                name = "Ống nhôm làm chuông gió (Bộ 5 ống)", description = "Ống nhôm đúc nguyên khối, âm thanh thanh thúy vang xa, cắt sẵn độ dài chuẩn để làm chuông gió DIY.",
//                price = 45000.0, quantity = 100, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_02", category_id = catMaterial1.category_id, category_name = catMaterial1.name,
//                name = "Vòng gỗ tròn treo chuông gió", description = "Vòng gỗ mộc tự nhiên đục sẵn lỗ đối xứng, chịu lực tốt, lý tưởng làm phần đỉnh treo cố định cho chuông gió handmade.",
//                price = 15000.0, quantity = 50, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_03", category_id = catMaterial1.category_id, category_name = catMaterial1.name,
//                name = "Dây cước tàng hình siêu bền", description = "Cuộn 50m dây cước trong suốt dẻo dai, chịu lực cao, chuyên dùng xỏ hạt cườm hoặc treo các thanh ống nhôm chuông gió.",
//                price = 10000.0, quantity = 200, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_04", category_id = catMaterial1.category_id, category_name = catMaterial1.name,
//                name = "Hạt cườm gỗ trang trí (Gói 100g)", description = "Hạt gỗ mộc mạc nhiều kích thước, dùng mài nhẵn bọc dây treo chuông gió hoặc làm vòng tay vintage phong cách mộc.",
//                price = 25000.0, quantity = 150, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_05", category_id = catMaterial1.category_id, category_name = catMaterial1.name,
//                name = "Hạt nhựa pha lê lấp lánh (Gói 50 hạt)", description = "Bắt sáng cực tốt, dùng làm điểm nhấn phản quang lấp lánh khi treo chiếc chuông gió của bạn ngoài ban công đón nắng.",
//                price = 18000.0, quantity = 80, images = placeholderImg, created_at = Timestamp.now()
//            ))
//
//            // --- DANH MỤC 2: DỤNG CỤ CHẾ TÁC (Phục vụ cắt keo, bấm chốt chuông gió) ---
//            products.add(Product(
//                product_id = "prod_06", category_id = catMaterial2.category_id, category_name = catMaterial2.name,
//                name = "Kéo cắt dây cước mini sắc bén", description = "Mũi kéo nhọn bằng thép không gỉ, bấm đứt các loại chỉ thêu, len sợi hoặc dây cước xỏ chuông gió cực kỳ mượt mà.",
//                price = 12000.0, quantity = 60, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_07", category_id = catMaterial2.category_id, category_name = catMaterial2.name,
//                name = "Bộ 10 tua vít sửa chữa mini", description = "Bộ tua vít bọc thép đa năng, đầu hít từ tính mạnh. Chuyên mở ốc nhỏ cho điện thoại, board mạch hoặc chế cháo linh kiện điện tử.",
//                price = 85000.0, quantity = 40, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_08", category_id = catMaterial2.category_id, category_name = catMaterial2.name,
//                name = "Keo dán đa năng siêu dính UHU", description = "Dán chắc chắn trên mọi chất liệu gỗ, kim loại, nhựa. Thích hợp cố định nút thắt dây cước để chuông gió không lo bị tuột gãy.",
//                price = 20000.0, quantity = 120, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_09", category_id = catMaterial2.category_id, category_name = catMaterial2.name,
//                name = "Kìm kẹp hạt xỏ kim loại mini", description = "Thiết kế đầu kìm nhỏ gọn, giúp kẹp chặt các chốt chặn kim loại cố định khoảng cách hạt cườm trên dây chuông gió.",
//                price = 40000.0, quantity = 30, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_10", category_id = catMaterial2.category_id, category_name = catMaterial2.name,
//                name = "Súng bắn keo nến mini 20W", description = "Gia nhiệt nhanh, chống tràn keo, đi kèm 2 cây keo nến. Phù hợp gắn kết hoa vải, đính phụ kiện đồ handmade mộc mạc.",
//                price = 55000.0, quantity = 85, images = placeholderImg, created_at = Timestamp.now()
//            ))
//
//            // --- DANH MỤC 3: ĐỒ TRANG TRÍ THỦ CÔNG (Thành phẩm) ---
//            products.add(Product(
//                product_id = "prod_11", category_id = catProduct1.category_id, category_name = catProduct1.name,
//                name = "Chuông gió gỗ mộc Nhật Bản", description = "Chuông gió làm sẵn từ thanh tre gốm, âm thanh nhẹ nhàng thư giãn mang lại cảm giác bình yên thanh tịnh.",
//                price = 150000.0, quantity = 20, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_12", category_id = catProduct1.category_id, category_name = catProduct1.name,
//                name = "Đèn ngủ len Macrame phong cách Boho", description = "Đan tay 100% từ sợi cotton tự nhiên, kết hợp bóng led vàng ấm áp tạo không gian lãng mạn cho phòng ngủ.",
//                price = 220000.0, quantity = 15, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_13", category_id = catProduct1.category_id, category_name = catProduct1.name,
//                name = "Cây thông kẽm nhung mini để bàn", description = "Sản phẩm uốn kẽm định hình thủ công tỉ mỉ, thích hợp trang trí bàn làm việc hoặc làm quà tặng mùa Giáng Sinh.",
//                price = 65000.0, quantity = 50, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_14", category_id = catProduct1.category_id, category_name = catProduct1.name,
//                name = "Tượng gốm mèo lười tráng men", description = "Tượng gốm mini tạo hình chú mèo ngái ngủ độc bản, nung lò thủ công truyền thống.",
//                price = 95000.0, quantity = 12, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_15", category_id = catProduct1.category_id, category_name = catProduct1.name,
//                name = "Khung tranh hoa cỏ sấy khô nghệ thuật", description = "Ép hoa lá thảo mộc tự nhiên sấy khô ép kính phẳng, bọc khung gỗ sồi sang trọng tinh tế.",
//                price = 180000.0, quantity = 8, images = placeholderImg, created_at = Timestamp.now()
//            ))
//
//            // --- DANH MỤC 4: TRANG SỨC HANDMADE (Thành phẩm) ---
//            products.add(Product(
//                product_id = "prod_16", category_id = catProduct2.category_id, category_name = catProduct2.name,
//                name = "Vòng tay pha lê xanh đại dương", description = "Xâu chuỗi phối màu từ các viên đá pha lê lấp lánh, tôn lên vẻ nữ tính dịu dàng cho bạn nữ.",
//                price = 45000.0, quantity = 60, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_17", category_id = catProduct2.category_id, category_name = catProduct2.name,
//                name = "Dây chuyền resin mặt trăng ôm đá", description = "Chất liệu keo epoxy trong suốt đúc phôi bạc 925, có khả năng hấp thụ ánh sáng phát quang nhẹ trong tối.",
//                price = 115000.0, quantity = 25, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_18", category_id = catProduct2.category_id, category_name = catProduct2.name,
//                name = "Khuyên tai kẽm nhung hoa hướng dương", description = "Sản phẩm thủ công cá tính, trọng lượng siêu nhẹ, không gây dị ứng hoặc đau tai khi đeo lâu.",
//                price = 35000.0, quantity = 40, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_19", category_id = catProduct2.category_id, category_name = catProduct2.name,
//                name = "Lắc chân vỏ ốc biển đan dây Boho", description = "Lắc chân bện thắt nút chỉ dù kết hợp vỏ ốc tự nhiên, mang đậm vibes hơi thở mùa hè bãi biển.",
//                price = 55000.0, quantity = 30, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_20", category_id = catProduct2.category_id, category_name = catProduct2.name,
//                name = "Nhẫn đính cườm hạt nhỏ pastel", description = "Kết từ dây chun co giãn mềm mại phối cụm hoa cúc nhỏ xinh, phù hợp làm nhẫn đôi bạn thân.",
//                price = 15000.0, quantity = 100, images = placeholderImg, created_at = Timestamp.now()
//            ))
//
//            // --- DANH MỤC 5: QUÀ TẶNG SÁNG TẠO (Thành phẩm) ---
//            products.add(Product(
//                product_id = "prod_21", category_id = catProduct3.category_id, category_name = catProduct3.name,
//                name = "Hộp nhạc gỗ sồi quay tay cơ học", description = "Hộp phát nhạc cơ khí cổ điển bằng chuyển động bánh răng, âm vang ấm áp thanh thoát.",
//                price = 280000.0, quantity = 10, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_22", category_id = catProduct3.category_id, category_name = catProduct3.name,
//                name = "Nến thơm tinh dầu sáp đậu nành", description = "Đổ tay thủ công, trang trí hoa khô nghệ thuật trên bề mặt sáp. Hương Lavender dịu nhẹ hỗ trợ ngủ ngon và xả stress.",
//                price = 85000.0, quantity = 45, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_23", category_id = catProduct3.category_id, category_name = catProduct3.name,
//                name = "Sổ tay da thuộc Vintage tự khâu bìa", description = "Bìa bọc da bò thật dập sần khâu chỉ gai thô mộc, bên trong là ruột giấy kraft chống lóa cổ kính.",
//                price = 160000.0, quantity = 18, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_24", category_id = catProduct3.category_id, category_name = catProduct3.name,
//                name = "Móc khóa len ếch xanh Amigurumi", description = "Móc tay len sợi cotton nhồi bông gòn êm ái, thích hợp treo cặp sách, balo hoặc chìa khóa xe máy siêu cưng.",
//                price = 35000.0, quantity = 70, images = placeholderImg, created_at = Timestamp.now()
//            ))
//            products.add(Product(
//                product_id = "prod_25", category_id = catProduct3.category_id, category_name = catProduct3.name,
//                name = "Hộp quà Blind Box nguyên liệu bí ẩn", description = "Hộp quà ngẫu nhiên chứa các nguyên vật liệu bí mật kèm hướng dẫn để bạn tự trải nghiệm bất ngờ tạo ra món đồ handmade.",
//                price = 99000.0, quantity = 50, images = placeholderImg, created_at = Timestamp.now()
//            ))
//
//            // Đẩy toàn bộ 25 sản phẩm vào Batch và commit lên Firestore 1 lần duy nhất
//            products.forEach { prod ->
//                val prodRef = db.collection("products").document(prod.product_id)
//                batch.set(prodRef, prod)
//            }
//
//            batch.commit().await()
//            ResultState(data = true)
//
//        } catch (e: Exception) {
//            ResultState(errorMessage = e.message ?: "Lỗi khi Seed dữ liệu")
//        }
//    }
//}