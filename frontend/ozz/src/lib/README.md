# @sjw-project/demo-header

`@sjw-project/demo-header`는 `sjw-project.site` 데모 서비스들이 공통으로 사용할 수 있는 React 헤더 라이브러리입니다. 헤더는 데모 프로젝트 링크를 보여주고, 공통 인증 서버의 `/api/projects`, `/api/me`, `/api/demo-login`, `/api/logout` API를 호출해 프로젝트 목록과 임시 로그인 상태를 표시합니다.

## 요구사항

- React 18 이상
- React DOM 18 이상
- 브라우저에서 인증 서버 쿠키를 주고받을 수 있는 CORS 및 쿠키 설정
- 기본 인증 서버: `https://auth.sjw-project.site`

## 설치

사용할 React 프로젝트에서 tarball을 설치합니다.

```bash
npm install /path/to/sjw-project-demo-header-0.1.2.tgz
```

예를 들어 같은 서버의 다른 프로젝트에서 설치한다면 실제 tarball 경로를 넘기면 됩니다.

```bash
npm install /home/developer/projects/demo-auth/demo-auth/frontend/DemoHeader/sjw-project-demo-header-0.1.2.tgz
```

## 기본 사용법

라우터보다 바깥, 앱 최상단에 `DemoHeader`를 배치합니다. CSS 파일도 함께 import해야 스타일이 적용됩니다. 기본 상태는 접힘입니다.

```jsx
import DemoHeader from "@sjw-project/demo-header";
import "@sjw-project/demo-header/style.css";

export default function App() {
  return (
    <>
      <DemoHeader />
      {/* routes */}
    </>
  );
}
```

처음부터 펼친 상태로 보여줘야 하면 `defaultExpanded` 사용.

```jsx
<DemoHeader defaultExpanded />
```

## 인증 서버 변경

기본값은 `https://auth.sjw-project.site`입니다. 다른 인증 서버를 사용해야 하면 `apiBaseUrl`을 넘깁니다.

```jsx
<DemoHeader apiBaseUrl="https://auth.sjw-project.site" />
```

헤더는 다음 API를 호출합니다.

- `GET {apiBaseUrl}/api/projects`
- `GET {apiBaseUrl}/api/me`
- `POST {apiBaseUrl}/api/demo-login`
- `POST {apiBaseUrl}/api/logout`

모든 요청은 `credentials: "include"`로 실행되므로 인증 서버는 쿠키 기반 인증과 cross-site 요청 설정을 올바르게 제공해야 합니다.

## 프로젝트 링크 관리

기본값은 인증 서버의 `GET /api/projects` 응답입니다. 데모가 늘어나면 라이브러리를 다시 빌드하지 말고 auth 서버의 `DEMO_PROJECTS` 환경변수만 수정합니다.

```env
DEMO_PROJECTS=[{"name":"COCO","url":"https://coco.sjw-project.site"},{"name":"Demo A","url":"https://demo-a.sjw-project.site"}]
```

특정 앱에서만 목록을 덮어써야 하면 `projects` prop 사용.

```jsx
const projects = [
  { name: "COCO", url: "https://coco.sjw-project.site" },
  { name: "Demo A", url: "https://demo-a.sjw-project.site" },
];

export default function App() {
  return <DemoHeader projects={projects} />;
}
```

현재 브라우저의 `window.location.host`와 링크 URL의 host가 같으면 해당 링크에 현재 페이지 스타일이 적용됩니다.

## Next.js에서 사용

Next.js App Router에서 사용할 경우 이 컴포넌트는 클라이언트 컴포넌트 안에서 렌더링해야 합니다.

```tsx
"use client";

import DemoHeader from "@sjw-project/demo-header";
import "@sjw-project/demo-header/style.css";

export function Header() {
  return <DemoHeader />;
}
```

그다음 `app/layout.tsx` 같은 서버 컴포넌트에서는 클라이언트 래퍼를 import해 배치합니다.

```tsx
import { Header } from "./Header";

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <body>
        <Header />
        {children}
      </body>
    </html>
  );
}
```

## 앱 상태와 동기화하기

COCO처럼 기존 Redux/store 로그인 상태와 데모 인증 상태를 동기화해야 하는 앱은 기본 `DemoHeader` 대신 `useDemoAuth`와 `DemoHeaderView`를 조합해 래퍼 컴포넌트를 만듭니다.

```jsx
import {
  DemoHeaderView,
  createDemoAuthClient,
  useDemoAuth,
} from "@sjw-project/demo-header";
import "@sjw-project/demo-header/style.css";

const authClient = createDemoAuthClient("https://auth.sjw-project.site");

export function AppDemoHeader() {
  const {
    me,
    loading,
    busy,
    error,
    handleDemoLogin,
    handleLogout,
  } = useDemoAuth({
    authClient,
    onMeChanged(nextMe) {
      // 여기에 Redux, Zustand, Context 등 앱 내부 상태 동기화 로직을 둡니다.
      console.log(nextMe);
    },
  });

  return (
    <DemoHeaderView
      me={me}
      loading={loading}
      busy={busy}
      error={error}
      onDemoLogin={handleDemoLogin}
      onLogout={handleLogout}
    />
  );
}
```

## 직접 인증 클라이언트 제공하기

인증 API 경로나 호출 방식이 다른 프로젝트는 `DemoAuthClient` 형태의 객체를 직접 만들 수 있습니다.

```jsx
const authClient = {
  async fetchMe() {
    const response = await fetch("/auth/me", { credentials: "include" });
    return response.json();
  },
  async fetchProjects() {
    const response = await fetch("/auth/projects", { credentials: "include" });
    const data = await response.json();
    return data.projects;
  },
  async login() {
    await fetch("/auth/demo-login", {
      method: "POST",
      credentials: "include",
    });
  },
  async logout() {
    await fetch("/auth/logout", {
      method: "POST",
      credentials: "include",
    });
  },
  subscribe(listener) {
    window.addEventListener("demo-auth-changed", listener);
    return () => window.removeEventListener("demo-auth-changed", listener);
  },
};
```

`subscribe`는 선택 사항입니다. 다른 탭, iframe, 앱 내부 이벤트 등으로 로그인 상태가 바뀔 때 헤더를 다시 조회해야 하는 경우에만 제공합니다.

## 공개 API

```ts
import DemoHeader from "@sjw-project/demo-header";
import {
  DemoHeaderStandalone,
  DemoHeaderView,
  createDemoAuthClient,
  useDemoAuth,
} from "@sjw-project/demo-header";
import "@sjw-project/demo-header/style.css";
```

- `DemoHeader`: 기본 export입니다. `DemoHeaderStandalone`과 같습니다.
- `DemoHeaderStandalone`: 기본 인증 클라이언트를 사용하는 완성형 헤더입니다. 기본은 접힘이며 `defaultExpanded`로 초기 펼침 가능.
- `DemoHeaderView`: 상태와 핸들러를 prop으로 받는 순수 UI 컴포넌트입니다.
- `createDemoAuthClient(apiBaseUrl?)`: 기본 인증 API 호출 클라이언트를 만듭니다.
- `useDemoAuth({ authClient, onMeChanged? })`: 인증 상태 조회, 로그인, 로그아웃 흐름을 관리합니다.

## 타입

패키지는 `dist/index.d.ts`를 포함합니다. 주요 타입은 다음과 같습니다.

```ts
type DemoProject = {
  name: string;
  url: string;
};

type DemoMe = {
  loggedIn: boolean;
  user: {
    id: string;
    nickname: string;
    role: "DEMO";
  } | null;
};
```

## 문제 해결

- 스타일이 적용되지 않으면 `import "@sjw-project/demo-header/style.css";`가 있는지 확인합니다.
- 프로젝트 링크가 비어 있으면 auth 서버의 `GET /api/projects` 응답과 `DEMO_PROJECTS` 환경변수를 확인합니다.
- 로그인 상태가 계속 `비로그인`이면 인증 서버의 쿠키 도메인, `SameSite=None`, `Secure`, CORS `credentials` 설정을 확인합니다.
- Next.js에서 `window is not defined` 문제가 나면 서버 컴포넌트에서 직접 렌더링하지 말고 `"use client"` 래퍼 안에서 렌더링합니다.
- tarball 설치 후 수정 사항이 반영되지 않으면 `frontend/DemoHeader`에서 `npm run pack:local`을 다시 실행하고, 사용하는 프로젝트에서 생성된 `.tgz`를 다시 설치합니다.
