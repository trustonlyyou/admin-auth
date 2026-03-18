# Levely Admin API

## 개요

### 회원가입
관리자 회원정보 입력  
→ 계정 생성  
→ OTP Secret 생성  
→ QR Code 발급  
→ Google Authenticator 등록  
→ OTP 인증 완료 시 가입 완료

### 로그인
ID/PW 입력  
→ 1차 인증 성공  
→ OTP 입력  
→ OTP 검증 성공  
→ AccessToken / RefreshToken 발급

### 이후
Node 관리자 페이지  
→ Bearer AccessToken으로 관리자 API 호출  
→ 만료 시 RefreshToken으로 재발급

---

## 1. 회원가입

회원가입은 총 2단계로 나뉘어 진다.

### 1.1 회원가입 1단계 : 계정생성 + OTP Secret 발급

- 1단계 - 계정생성 + OTP Secret 발급
- 2단계 - OTP 검증 후 가입 완료

#### 회원가입 요청
- **POST**
- **/api/v1/admin/auth/signup/init**

```json
{
  "email": "admin@levely.com",
  "password": "password123!",
  "name": "오정환",
  "phone": "01068300772"
}
```

#### 회원가입 응답

```json
{
  "adminId": 1,
  "email": "admin@levely.com",
  "otpSecretIssued": true,
  "otpAuthUrl": "otpauth://totp/Levely:admin@levely.com?secret=ABC123DEF456&issuer=Levely",
  "status": "PENDING"
}
```

#### 1.1.1 회원가입 1단계 처리 로직

- 이메일 / 휴대폰 중복체크
- 비밀번호 암호화
- OTP Secret 생성
- 관리자 계정 임시 생성
- QR 등록용 otpauth URL 생성
- 가입 상태를 `PENDING` 으로 저장

### 1.2 회원가입 2단계 : OTP 검증 후 가입 완료

#### 회원가입 요청
- **POST**
- **/api/v1/admin/auth/signup/verify-otp**

```json
{
  "adminId": 1,
  "otpCode": "123456"
}
```

#### 회원가입 응답

```json
{
  "adminId": 1,
  "email": "admin@levely.com",
  "status": "ACTIVE",
  "message": "관리자 회원가입이 완료되었습니다."
}
```

#### 1.2.1 회원가입 2단계 처리 로직

- 저장된 OTP Secret 조회
- 입력한 OTP 코드 검증
- 성공 시 상태를 `ACTIVE` 로 변경
- `otpEnabled = true` 처리
- 가입 완료

---

## 2. 로그인

로그인은 총 2단계로 나뉘어 진다.

### 2.1 로그인 1단계 : 이메일 / 비밀번호 검증

- 1단계 - 이메일 / 비밀번호 검증
- 2단계 - OTP 검증 후 JWT 발급

#### 로그인 요청 1단계
- **POST**
- **/api/v1/admin/auth/login**

```json
{
  "email": "admin@levely.com",
  "password": "password123!"
}
```

#### 로그인 1단계 응답

```json
{
  "otpRequired": true,
  "loginToken": "temporary-login-token",
  "message": "OTP 인증이 필요합니다."
}
```

### 2.2 로그인 2단계 : OTP 검증 후 JWT 발급

#### 로그인 요청 2단계
- **POST**
- **/api/v1/admin/auth/login/verify-otp**

```json
{
  "loginToken": "temporary-login-token",
  "otpCode": "123456"
}
```

#### 로그인 2단계 응답

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 1800
}
```

#### 2.2.2 로그인 2단계 처리 로직

- `loginToken` 유효성 검증
- 해당 관리자 계정 확인
- OTP 코드 검증
- 성공 시 AccessToken / RefreshToken 발급
- 로그인 이력 저장

---

## 3. 토큰

### 3.1 Access Token

- 권한이 필요한 API 호출에 사용
- 만료시간 : 30분

```json
{
  "adminId": 1,
  "email": "admin@levely.com",
  "role": "SUPER_ADMIN",
  "type": "ACCESS"
}
```

### 3.2 RefreshToken

- AccessToken 재발급
- 만료시간 : 7일 ~ 14일

```json
{
  "adminId": 1,
  "type": "REFRESH"
}
```

---

## 4. 재발급

#### 재발급 요청
- **POST**
- **/api/v1/admin/auth/refresh**

```json
{
  "refreshToken": "..."
}
```

#### 재발급 응답

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 1800
}
```

### 4.1 재발급 처리로직

- RefreshToken 검증
- 저장소(DB/Redis)와 비교
- 성공 시 새 토큰 발급
- RefreshToken rotation 적용 가능

---

## 5. 로그아웃

#### 로그아웃 요청
- **POST**
- **/api/v1/admin/auth/logout**

### 5.1 로그아웃 처리로직

- RefreshToken 삭제 또는 무효화
- 필요 시 AccessToken 블랙 리스트 처리
- 로그인 세션 종료
