<template>
	<view class="container">
		<view v-if="showPrizeModal" class="mask"></view>
		<view v-if="showPrizeModal" class="modal">
			<view class="modal-content">
				<text class="modal-message">{{ todayPrize }}</text>
				<button class="modal-button" @click="closePrizeModal">确定</button>
			</view>
		</view>

		<view class="wheel-wrapper">
			<!-- Canvas 转盘 -->
			<canvas canvas-id="wheelCanvas" class="wheel-canvas"
				:style="{ width: canvasPixelSize + 'px', height: canvasPixelSize + 'px' }"></canvas>

			<!-- 指针（固定在顶部，指向转盘中心上方） -->
			<view class="pointer"></view>
		</view>

		<!-- 开始抽奖按钮 -->
		<button @click="startSpin" :disabled="isSpinning" v-if="showSpin"  class="spin-button">
			{{ isSpinning ? '转动中...' : '开始转动' }}
		</button>

		<!-- 中奖结果显示 -->
		<view v-if="resultText" class="result">{{ resultText }}</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				// 🎯 奖品列表（可自定义）
				prizes: [],

				// 🎨 每个扇形的颜色（循环使用）
				colors: [],

				// 📐 Canvas 相关
				canvasPixelSize: 300, // Canvas 宽高（像素）
				ctx: null, // Canvas 绘图上下文
				totalRotation: 0, // 当前累积旋转角度（用于控制转盘视觉旋转）
				isSpinning: false, // 是否正在旋转
				animationId: null, // requestAnimationFrame ID

				//结果文本
				resultText: '',
				resultPrize: '',

				//今天吃了啥
				todayPrize: '',
				showPrizeModal: false,
				showSpin: true
			};
		},

		mounted() {
			this.loadWheelConfig();
		},

		beforeDestroy() {
			// 组件销毁前清除动画
			if (this.animationId) {
				cancelAnimationFrame(this.animationId);
			}
		},

		methods: {
			async loadWheelConfig() {
				try {
					const res = await uni.request({
						url: 'http://localhost:8080/api/wheel/getPrizeList',
						method: 'GET'
					});
					if (res.statusCode === 200 && res.data) {
						const prize = res.data;
						this.prizes = prize.map(item => item.name);
						this.colors = prize.map(item => item.color);
					}
				} catch (e) {
					console.error('数据加载失败', e);
					uni.showToast({
						title: '数据加载失败',
						icon: 'none'
					})
				}
				this.initCanvas();
				this.getTodayPriz();
			},

			async getTodayPriz() {
				try {
					const res = await uni.request({
						url: 'http://localhost:8080/api/wheel/getTodayPrize',
						method: 'GET'
					});
					if (res.statusCode === 200 && res.data) {
						this.todayPrize = res.data;
						this.resultText = "今天吃："+this.todayPrize+"不能再变了！";
						this.showSpin = false;
					}
				} catch (e) {
					console.error('数据加载失败', e);
					uni.showToast({
						title: '数据加载失败',
						icon: 'none'
					})
				}
			},
			// 🛠 初始化 Canvas，获取绘图上下文并绘制转盘
			initCanvas() {
				this.ctx = uni.createCanvasContext('wheelCanvas', this);
				this.drawWheel();
			},

			// 🎨 绘制转盘：扇形 + 文字
			drawWheel() {
				if (!this.ctx) return;
				const centerX = this.canvasPixelSize / 2;
				const centerY = this.canvasPixelSize / 2;
				const radius = this.canvasPixelSize / 2 - 15; // 留出空间避免文字溢出
				const prizeCount = this.prizes.length;
				const anglePerPrize = (2 * Math.PI) / prizeCount;

				// 清空画布
				//this.ctx.clearRect(0, 0, this.canvasPixelSize, this.canvasPixelSize);
				for (let i = 0; i < prizeCount; i++) {
					const baseOffset = -Math.PI / 2; // 🎯 偏移 -90°，让扇形 0° 指向 12 点钟（指针方向）

					const startAngle = (i * anglePerPrize + this.totalRotation * Math.PI / 180) + baseOffset;
					const endAngle = startAngle + anglePerPrize;

					// 1. 绘制扇形
					this.ctx.beginPath();
					this.ctx.moveTo(centerX, centerY);
					this.ctx.arc(centerX, centerY, radius, startAngle, endAngle);
					this.ctx.closePath();

					this.ctx.fillStyle = this.colors[i % this.colors.length];
					this.ctx.fill();

					// 2. 绘制文字（如“鱼香肉丝”）
					this.ctx.save();
					this.ctx.translate(centerX, centerY);
					this.ctx.rotate(startAngle + anglePerPrize / 2);
					this.ctx.textAlign = 'center';
					this.ctx.fillStyle = '#ffffff';
					this.ctx.font = '20px sans-serif';
					this.ctx.fillText(this.prizes[i], radius * 0.7, 4);
					this.ctx.restore();
				}
				// 必须调用 draw() 才会将内容渲染到画布上！
				this.ctx.draw();
			},

			// 🎯 开始转盘旋转
			startSpin() {
				if (this.isSpinning) return;

				this.isSpinning = true;
				this.resultText = '';

				const prizeCount = this.prizes.length;
				const anglePerPrize = 360 / prizeCount;

				// 随机选择一个
				const selectedIndex = Math.floor(Math.random() * prizeCount);
				const targetAngle = selectedIndex * anglePerPrize + anglePerPrize / 2;

				// 总共转 N 圈 + 目标角度的反方向（因为指针固定在顶部）
				const spins = 100; // 转 5 圈
				const finalAngle = spins * 360 + (360 - targetAngle);
				// 执行旋转动画
				this.doRotationAnimation(finalAngle, () => {
					// 动画结束后显示中奖
					this.resultText = `😀今天中午吃：${this.prizes[selectedIndex]}`;
					this.resultPrize = `${this.prizes[selectedIndex]}`;
					this.isSpinning = false;
					this.saveEatWhat();
				});

			},
			async saveEatWhat() {
				try {
					const res = await uni.request({
						//url: 'http://175.178.2.14:8080/api/wheel/saveEatWhat',
						url: 'http://localhost:8080/api/wheel/saveEatWhat',
						method: 'POST',
						data: {
							name: this.resultPrize
						},
						header: {
							'Content-Type': 'application/json'
						}
					});
					if (res.statusCode === 200 && res.data) {
						console.log('保存吃了啥成功！');
					}
				} catch (e) {
					console.error('保存吃了啥失败！', e);
				}
				this.initCanvas();
			},
			// 🌀 执行旋转动画（使用 requestAnimationFrame）
			doRotationAnimation(targetTotalAngle, onFinish) {
				let current = 0;
				const totalTarget = targetTotalAngle;
				const duration = 4000; // 动画时长 4 秒
				const startTime = Date.now();

				const step = () => {
					const elapsed = Date.now() - startTime;
					let progress = Math.min(elapsed / duration, 1);

					// 缓动函数：先快后慢
					progress = 1 - Math.pow(1 - progress, 3); // easeOut cubic

					const currentAngle = totalTarget * progress;
					this.totalRotation = currentAngle;

					// 重绘转盘（旋转后效果）
					this.drawWheel();

					if (progress < 1) {
						this.animationId = requestAnimationFrame(step);
					} else {
						this.totalRotation = totalTarget; // 确保最终停留在目标角度，避免浮点误差
						this.drawWheel(); // 最后重绘一次
						if (onFinish) onFinish(); // 🎯 调用回调，显示结果
					}
				};

				step();
			},
			// 关闭弹窗
			closePrizeModal() {
				this.showPrizeModal = false;
			}
		},
	};
</script>

<style scoped>
	/* 转盘 */
	.container {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 20px;
	}

	.wheel-wrapper {
		position: relative;
		margin-bottom: 40px;
	}

	.wheel-canvas {
		border: 3px solid #ddd;
		border-radius: 50%;
		box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
	}

	.pointer {
		position: absolute;
		top: -10px;
		left: 50%;
		transform: translateX(-50%);
		width: 0;
		height: 0;
		border-left: 10px solid transparent;
		border-right: 10px solid transparent;
		border-top: 20px solid #ff4757;
		z-index: 10;
	}

	.spin-button {
		background-color: #ff6b6b;
		color: white;
		border: none;
		padding: 12px 24px;
		font-size: 16px;
		border-radius: 25px;
		margin-bottom: 20px;
	}

	.spin-button[disabled] {
		background-color: #ccc;
	}

	.result {
		font-size: 18px;
		font-weight: bold;
		color: #333;
		margin-top: 20px;
	}

	/* 弹窗 */
	.mask {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background-color: rgba(0, 0, 0, 0.6);
		z-index: 999;
	}

	.modal {
		position: fixed;
		top: 50%;
		left: 50%;
		transform: translate(-50%, -50%);
		background-color: #ffffff;
		padding: 40rpx;
		border-radius: 16rpx;
		z-index: 1000;
		width: 600rpx;
		text-align: center;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.2);
	}

	.modal-message {
		font-size: 32rpx;
		color: #333333;
		margin-bottom: 40rpx;
	}

	.modal-button {
		background-color: #007aff;
		color: #ffffff;
		border: none;
		border-radius: 8rpx;
		padding: 20rpx 40rpx;
		font-size: 32rpx;
	}
</style>