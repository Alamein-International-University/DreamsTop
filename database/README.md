# 🗄️ DreamsTop (i-Wish) - Database Documentation & Guide

**Role:** Omar ElSharkawy ([@omarehab544](https://github.com/omarehab544))  
**Component:** Database Design, DAO Layer & Schema  
**Target RDBMS:** MySQL 8.x (InnoDB engine, `utf8mb4_unicode_ci`)

---

## 📂 ملفات قاعدة البيانات (Database Files)

| الملف | الوصف |
| :--- | :--- |
| **[`schema.sql`](schema.sql)** | ملف الـ DDL الكامل: ينشئ قاعدة بيانات `dreamstop_db` والـ 6 جداول والقيود والـ Foreign Keys والـ Indexes. |
| **[`seed.sql`](seed.sql)** | ملف الـ DML: يحتوي على البيانات الأولية للتيم، الـ 18 منتج متجر، العلاقات، الأمنيات، والمساهمات. |
| **[`backup_dreamstop_db.sql`](backup_dreamstop_db.sql)** | ملف النسخة الاحتياطية والتثبيت السريع (Turnkey Setup): يجمع الـ Schema والـ Seed معاً جاهزاً للتسليم والمناقشة. |

---

## 🏗️ هيكل الجداول والعلاقات (Tables & Relationships)

### 1. جدول المستخدمين (`users`)
* **الهدف:** إدارة حسابات المستخدمين، تسجيل الدخول، الرصيد المالي، والبيانات الشخصية.
* **الأعمدة:**
  * `id`: المفتاح الأساسي (INT Auto-Increment).
  * `username`: اسم المستخدم الفريد (UNIQUE).
  * `email`: البريد الإلكتروني الفريد (UNIQUE).
  * `password_hash`: الرمز المشفر لكلمة المرور (SHA-256).
  * `full_name`: الاسم الكامل.
  * `balance`: رصيد المحفظة الحالي (`DECIMAL(12, 2)` مع قيد `CHECK (balance >= 0)`).
  * `avatar_color`: لون البروفايل الرمزي.
  * `bio`: نبذة تعريفية.
  * `created_at`, `updated_at`: طوابع زمنية.

### 2. جدول المنتجات والكتالوج (`items`)
* **الهدف:** تخزين قائمة المنتجات المتاحة في المتجر (كروت شاشة، كونسول، ألعاب، إكسسوارات) التي يختار منها المستخدمون لإضافتها لأمنياتهم.
* **الأعمدة:**
  * `id`: المفتاح الأساسي.
  * `name`: اسم المنتج.
  * `description`: وصف المنتج ومواصفاته.
  * `category`: التصنيف (GPU, Console, Game, Peripherals, Monitor, Handheld, Steam).
  * `price`: السعر المالي (`DECIMAL(12, 2)` مع قيد `CHECK (price > 0)`).
  * `icon_emoji`: إيموجي رمزي للمنتج.

### 3. جدول علاقات الصداقة (`friendships`)
* **الهدف:** إدارة طلبات الصداقة وحالاتها وقوائم الأصدقاء.
* **الأعمدة والقيود:**
  * `requester_id`: معرف المستخدم المرسل للطلب (FK إلى `users(id)`).
  * `addressee_id`: معرف المستخدم المستقبل للطلب (FK إلى `users(id)`).
  * `status`: حالة الطلب (`PENDING`, `ACCEPTED`, `DECLINED`).
  * `CONSTRAINT chk_no_self_friend`: يمنع أن يرسل يوزر طلب صداقة لنفسه (`requester_id <> addressee_id`).
  * `UNIQUE (requester_id, addressee_id)`: يمنع تكرار إرسال طلب الصداقة بين نفس الشخصين.

### 4. جدول عناصر الأمنيات (`wishlist_items`)
* **الهدف:** ربط المستخدم بالمنتجات التي يتمناها، مع تتبع المبلغ المستهدف والمبلغ الذي تم جمعه حتى الآن.
* **الأعمدة والقيود:**
  * `user_id`: صاحب قائمة الأمنيات (FK إلى `users(id)` مع `ON DELETE CASCADE`).
  * `item_id`: المنتج المختار (FK إلى `items(id)` مع `ON DELETE CASCADE`).
  * `notes`: ملاحظات خاصة يكتبها المستخدم (مثل اللون أو الحجم المفضل).
  * `priority`: مستوى الأولوية (`LOW`, `MEDIUM`, `HIGH`).
  * `target_amount`: المبلغ المطلوب جمعه (غالباً سعر المنتج أو مبلغ مخصص).
  * `current_paid_amount`: المبلغ الإجمالي الذي ساهم به الأصدقاء حتى اللحظة.
  * `is_completed`: قيمة بوليانية تشير هل اكتمل تمويل الهدية 100% أم لا.

### 5. جدول المساهمات المالية (`contributions`)
* **الهدف:** تسجيل كل عملية دفع ومساهمة يقوم بها صديق تجاه عنصر في قائمة أمنيات صديقه.
* **الأعمدة والقيود:**
  * `contributor_id`: الصديق الذي دفع وساهم (FK إلى `users(id)`).
  * `wishlist_item_id`: عنصر الأمنية المستهدف (FK إلى `wishlist_items(id)`).
  * `amount`: المبلغ المدفوع (`CHECK (amount > 0)`).
  * `created_at`: تاريخ ووقت المساهمة.

### 6. جدول الإشعارات (`notifications`)
* **الهدف:** تخزين التنبيهات الموجهة للمستخدمين عند حدوث أحداث رئيسية.
* **الأنواع المدعومة (`type`):**
  * `FRIEND_REQUEST`: وصلك طلب صداقة جديد.
  * `FRIEND_REQUEST_ACCEPTED`: قبل فلان طلب صداقتك.
  * `CONTRIBUTION_RECEIVED`: ساهم صديق في إحدى أمنياتك.
  * `ITEM_COMPLETED_BUYER`: إشعار للمساهم باكتمل ثمن الهدية وجاهزيتها.
  * `ITEM_COMPLETED_RECEIVER`: إشعار لصاحب الأمنية باكتمال تمويل هديته بالكامل.

---

## 🚀 طريقة تشغيل وتثبيت قاعدة البيانات على MySQL

### الطريقة الأولى: من خلال MySQL Command Line
افتح التيرمينال أو الـ PowerShell واكتب:
```bash
mysql -u root -p < database/backup_dreamstop_db.sql
```
ثم أدخل كلمة مرور الـ root الخاصة بجهازك.

### الطريقة الثانية: من خلال MySQL Workbench
1. افتح **MySQL Workbench** واتصل بالـ Local Instance.
2. من القائمة العلوية اختر `File` -> `Open SQL Script...`.
3. اختر ملف `backup_dreamstop_db.sql` (أو `schema.sql` ثم `seed.sql`).
4. اضغط على أيقونة البرق (⚡ Execute) لتنفيذ السكربت بالكامل.
5. اعمل Refresh لقائمة الـ Schemas وستظهر لك قاعدة بيانات `dreamstop_db` بجميع جداولها وبياناتها.
