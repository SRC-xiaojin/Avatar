import type { DefineComponent } from 'vue'

// Vue组件props类型声明
declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    // 可以在这里添加全局属性的类型声明
  }
}

// Element Plus 组件类型增强
declare module 'element-plus' {
  // 这里可以添加Element Plus相关的类型增强
}

// 扩展Vue组件的类型
export interface VueComponent extends DefineComponent<{}, {}, any> {}

// JSPlumb相关类型声明
declare module 'jsplumb' {
  export interface jsPlumb {
    // JSPlumb相关方法的类型声明
    ready(fn: () => void): void
    setContainer(container: string | Element): void
    draggable(element: string | Element, options?: any): void
    connect(options: any): any
    // 可以根据实际使用的JSPlumb API添加更多类型声明
  }
  
  const jsPlumb: jsPlumb
  export default jsPlumb
}

// Monaco Editor相关类型扩展
declare module '@monaco-editor/loader' {
  interface LoaderOptions {
    paths?: { vs: string }
  }
  
  interface Loader {
    config(options: LoaderOptions): void
    init(): Promise<typeof import('monaco-editor')>
  }
  
  const loader: Loader
  export default loader
} 